package ye.gov.pmo.bootstrap.backfill;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.BackfillManifest.Action;
import ye.gov.pmo.bootstrap.backfill.BackfillManifest.Entry;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog.Snapshot;
import ye.gov.pmo.content.service.ContentHtmlSanitizer;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
public class ContentBackfillApplyService {
    public static final String CONFIRMATION = "APPLY_UNIFIED_CONTENT_V1";

    private static final ZoneOffset YEMEN_OFFSET = ZoneOffset.ofHours(3);
    private static final Map<String, Integer> ARABIC_MONTHS = Map.ofEntries(
            Map.entry("يناير", 1), Map.entry("فبراير", 2), Map.entry("مارس", 3),
            Map.entry("أبريل", 4), Map.entry("مايو", 5), Map.entry("يونيو", 6),
            Map.entry("يوليو", 7), Map.entry("أغسطس", 8), Map.entry("سبتمبر", 9),
            Map.entry("أكتوبر", 10), Map.entry("نوفمبر", 11), Map.entry("ديسمبر", 12));

    private final BackfillManifestLoader manifestLoader;
    private final LegacyContentSourceCatalog sourceCatalog;
    private final ContentBackfillReconciliationService reconciliation;
    private final BackfillCategoryCatalog categories;
    private final ContentHtmlSanitizer sanitizer;
    private final CurrentActorProvider actorProvider;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public ContentBackfillApplyService(BackfillManifestLoader manifestLoader,
                                       LegacyContentSourceCatalog sourceCatalog,
                                       ContentBackfillReconciliationService reconciliation,
                                       BackfillCategoryCatalog categories,
                                       ContentHtmlSanitizer sanitizer,
                                       CurrentActorProvider actorProvider,
                                       AuditService auditService,
                                       ObjectMapper objectMapper,
                                       JdbcTemplate jdbcTemplate) {
        this.manifestLoader = manifestLoader;
        this.sourceCatalog = sourceCatalog;
        this.reconciliation = reconciliation;
        this.categories = categories;
        this.sanitizer = sanitizer;
        this.actorProvider = actorProvider;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public BackfillApplyResponse apply(BackfillApplyRequest request) {
        BackfillManifest manifest = manifestLoader.load();
        validateConfirmation(request, manifest);
        lockPrimaryEntity(manifest.primaryEntityId());

        BackfillReconciliationReport preflight = reconciliation.reconcile();
        if (!preflight.readyToApply()) {
            throw new BackfillApplyException("Backfill preflight has " + preflight.counts().blockers() + " blocker(s)");
        }

        Long actorUserId = actorProvider.currentUserId();
        if (actorUserId == null) {
            throw new BackfillApplyException("An authenticated database user is required for backfill attribution");
        }

        Map<String, Snapshot> sources = sourceCatalog.discover().stream()
                .collect(Collectors.toMap(Snapshot::sourceKey, item -> item));
        Map<String, List<Entry>> groups = manifest.entries().stream()
                .filter(entry -> entry.action() != Action.SKIP_WITH_REASON)
                .collect(Collectors.groupingBy(Entry::canonicalKey, LinkedHashMap::new, Collectors.toList()));

        int createdItems = 0;
        int existingItems = 0;
        int createdMappings = 0;
        int existingMappings = 0;

        for (Map.Entry<String, List<Entry>> grouped : groups.entrySet()) {
            List<Entry> entries = grouped.getValue();
            Entry owner = entries.stream().filter(entry -> entry.action() == Action.CREATE)
                    .findFirst().orElseThrow(() -> new BackfillApplyException(
                            "No CREATE owner for canonical key " + grouped.getKey()));
            List<UUID> mappedSources = entries.stream().map(this::findMappedContentId)
                    .filter(Objects::nonNull).toList();
            Set<UUID> mappedItems = Set.copyOf(mappedSources);

            if (!mappedItems.isEmpty()) {
                if (mappedItems.size() != 1 || mappedSources.size() != entries.size()) {
                    throw new BackfillApplyException("Partial or conflicting mappings for " + grouped.getKey());
                }
                existingItems++;
                existingMappings += mappedSources.size();
                continue;
            }

            UUID contentItemId = createContentItem(manifest, owner, entries, sources, actorUserId);
            for (Entry entry : entries) {
                jdbcTemplate.update("""
                        insert into legacy_content_mappings
                            (id, source_system, source_type, legacy_id, content_item_id, created_at)
                        values (?, ?, ?, ?, ?, ?)
                        """, UUID.randomUUID(), entry.sourceSystem(), entry.sourceType(), entry.legacyId(),
                        contentItemId, OffsetDateTime.now());
                createdMappings++;
            }
            createdItems++;
        }

        int skipped = (int) manifest.entries().stream()
                .filter(entry -> entry.action() == Action.SKIP_WITH_REASON).count();
        auditService.record(actorUserId, "UNIFIED_CONTENT_BACKFILL_APPLY", "ContentBackfill",
                "manifest-v" + manifest.schemaVersion(), manifest.primaryEntityId(), AuditOutcome.SUCCESS,
                null, "createdItems=" + createdItems + ",createdMappings=" + createdMappings
                        + ",existingItems=" + existingItems + ",skippedSources=" + skipped);

        return new BackfillApplyResponse(manifest.schemaVersion(), createdItems > 0,
                createdItems, existingItems, createdMappings, existingMappings, skipped, Instant.now());
    }

    private UUID createContentItem(BackfillManifest manifest, Entry owner, List<Entry> entries,
                                   Map<String, Snapshot> sources, Long actorUserId) {
        Snapshot bodySource = entries.stream().map(Entry::sourceKey).map(sources::get)
                .filter(Objects::nonNull).filter(Snapshot::suppliesBody)
                .findFirst().orElseThrow(() -> new BackfillApplyException(
                        "No body source for " + owner.canonicalKey()));
        String byline = entries.stream().map(Entry::expectedByline)
                .filter(value -> value != null && !value.isBlank()).findFirst().orElse(null);
        OffsetDateTime sourceDate = parseSourceDate(owner.expectedDate());
        OffsetDateTime now = OffsetDateTime.now();
        UUID itemId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();

        jdbcTemplate.update("""
                insert into content_items
                    (id, content_type, primary_entity_id, slug, locale, status, display_metadata,
                     created_at, updated_at, created_by, updated_by, version)
                values (?, ?, ?, ?, 'ar', 'DRAFT', ?, ?, ?, ?, ?, 0)
                """, itemId, owner.sourceType(), manifest.primaryEntityId(), owner.slug(),
                displayMetadata(manifest, owner, entries), now, now, actorUserId, actorUserId);

        String body = sanitizer.sanitize("<p>" + escapeHtml(bodySource.summary()) + "</p>");
        jdbcTemplate.update("""
                insert into content_revisions
                    (id, content_item_id, revision_number, title, summary, body, byline,
                     change_note, created_at, created_by)
                values (?, ?, 1, ?, ?, ?, ?, ?, ?, ?)
                """, revisionId, itemId, owner.expectedTitle(), bodySource.summary(), body, byline,
                "Imported from reviewed unified-content-v1 manifest", now, actorUserId);

        String status = owner.expectedStatus();
        UUID publishedRevisionId = status.equals("PUBLISHED") ? revisionId : null;
        OffsetDateTime firstPublishedAt = status.equals("PUBLISHED") ? sourceDate : null;
        OffsetDateTime archivedAt = status.equals("ARCHIVED") ? sourceDate : null;
        jdbcTemplate.update("""
                update content_items
                set status = ?, current_revision_id = ?, published_revision_id = ?,
                    first_published_at = ?, last_published_at = ?, archived_at = ?,
                    updated_at = ?, updated_by = ?
                where id = ?
                """, status, revisionId, publishedRevisionId, firstPublishedAt, firstPublishedAt,
                archivedAt, now, actorUserId, itemId);

        entries.stream().map(Entry::expectedCategory).distinct().sorted()
                .forEach(label -> assignCategory(itemId, label, actorUserId, now));
        return itemId;
    }

    private void assignCategory(UUID itemId, String label, Long actorUserId, OffsetDateTime now) {
        String slug = categories.slugFor(label).orElseThrow(() ->
                new BackfillApplyException("No taxonomy slug approved for category " + label));
        List<UUID> existing = jdbcTemplate.query("""
                select id from taxonomy_terms where taxonomy_code = 'CONTENT_CATEGORY' and slug = ?
                """, (resultSet, row) -> resultSet.getObject(1, UUID.class), slug);
        UUID termId;
        if (existing.isEmpty()) {
            termId = UUID.randomUUID();
            jdbcTemplate.update("""
                    insert into taxonomy_terms
                        (id, taxonomy_code, slug, label_ar, active, created_at)
                    values (?, 'CONTENT_CATEGORY', ?, ?, true, ?)
                    """, termId, slug, label, now);
        } else {
            termId = existing.getFirst();
        }
        jdbcTemplate.update("""
                insert into content_taxonomy_assignments
                    (content_item_id, taxonomy_term_id, created_at, created_by)
                values (?, ?, ?, ?)
                """, itemId, termId, now, actorUserId);
    }

    private UUID findMappedContentId(Entry entry) {
        List<UUID> ids = jdbcTemplate.query("""
                select content_item_id from legacy_content_mappings
                where source_system = ? and source_type = ? and legacy_id = ?
                """, (resultSet, row) -> resultSet.getObject(1, UUID.class),
                entry.sourceSystem(), entry.sourceType(), entry.legacyId());
        if (ids.size() > 1) {
            throw new BackfillApplyException("Duplicate mapping for " + entry.sourceKey());
        }
        return ids.isEmpty() ? null : ids.getFirst();
    }

    private void validateConfirmation(BackfillApplyRequest request, BackfillManifest manifest) {
        if (!Objects.equals(request.manifestSchemaVersion(), manifest.schemaVersion())) {
            throw new BackfillApplyException("Confirmed manifest version does not match the loaded manifest");
        }
        if (!CONFIRMATION.equals(request.confirmation())) {
            throw new BackfillApplyException("Backfill confirmation phrase is invalid");
        }
    }

    private void lockPrimaryEntity(UUID entityId) {
        List<UUID> locked = jdbcTemplate.query(
                "select id from government_entities where id = ? for update",
                (resultSet, row) -> resultSet.getObject(1, UUID.class), entityId);
        if (locked.isEmpty()) {
            throw new BackfillApplyException("Manifest primary entity does not exist");
        }
    }

    private String displayMetadata(BackfillManifest manifest, Entry owner, List<Entry> entries) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("backfillManifestVersion", manifest.schemaVersion());
        metadata.put("canonicalKey", owner.canonicalKey());
        metadata.put("legacySources", entries.stream()
                .sorted(Comparator.comparing(Entry::sourceKey))
                .map(entry -> Map.of(
                        "sourceKey", entry.sourceKey(),
                        "sourceDate", entry.expectedDate(),
                        "category", entry.expectedCategory()))
                .toList());
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            throw new BackfillApplyException("Unable to serialize backfill provenance metadata");
        }
    }

    private OffsetDateTime parseSourceDate(String value) {
        try {
            return LocalDate.parse(value).atTime(LocalTime.NOON).atOffset(YEMEN_OFFSET);
        } catch (java.time.format.DateTimeParseException ignored) {
            String[] parts = value.split(" ");
            if (parts.length != 3 || !ARABIC_MONTHS.containsKey(parts[1])) {
                throw new BackfillApplyException("Unsupported legacy date: " + value);
            }
            LocalDate date = LocalDate.of(Integer.parseInt(parts[2]), ARABIC_MONTHS.get(parts[1]),
                    Integer.parseInt(parts[0]));
            return date.atTime(LocalTime.NOON).atOffset(YEMEN_OFFSET);
        }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
