package ye.gov.pmo.bootstrap.backfill;

import static ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport.Severity.BLOCKER;
import static ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport.Severity.WARNING;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.BackfillManifest.Action;
import ye.gov.pmo.bootstrap.backfill.BackfillManifest.Entry;
import ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport.DatabaseState;
import ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport.Issue;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog.Snapshot;

@Service
public class ContentBackfillReconciliationService {
    private static final Set<String> CONTENT_TYPES = Set.of("NEWS", "ANNOUNCEMENT", "DECISION", "DOCUMENT");
    private static final Set<String> CONTENT_STATUSES = Set.of("DRAFT", "IN_REVIEW", "APPROVED", "PUBLISHED", "ARCHIVED");
    private final BackfillManifestLoader manifestLoader;
    private final LegacyContentSourceCatalog sourceCatalog;
    private final BackfillCategoryCatalog categories;
    private final JdbcTemplate jdbcTemplate;

    public ContentBackfillReconciliationService(BackfillManifestLoader manifestLoader,
                                                LegacyContentSourceCatalog sourceCatalog,
                                                BackfillCategoryCatalog categories,
                                                JdbcTemplate jdbcTemplate) {
        this.manifestLoader = manifestLoader;
        this.sourceCatalog = sourceCatalog;
        this.categories = categories;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public BackfillReconciliationReport reconcile() {
        BackfillManifest manifest = manifestLoader.load();
        List<Snapshot> sources = sourceCatalog.discover();
        List<Issue> issues = new ArrayList<>();

        validateManifestHeader(manifest, issues);
        validatePrimaryEntity(manifest.primaryEntityId(), issues);
        Map<String, Snapshot> sourceByKey = indexSources(sources, issues);
        Map<String, Entry> entryBySource = indexEntries(manifest.entries(), issues);
        validateCoverage(sourceByKey, entryBySource, issues);
        validateSnapshots(sourceByKey, entryBySource, issues);
        validateActions(manifest.entries(), sourceByKey, issues);

        DatabaseState databaseState = inspectDatabase(manifest.entries());
        if (databaseState.orphanMappings() > 0) {
            issues.add(issue(BLOCKER, "ORPHAN_LEGACY_MAPPING", null, null,
                    "Legacy mappings contain references without a content item"));
        }
        if (databaseState.canonicalSlugCollisions() > 0) {
            issues.add(issue(BLOCKER, "CANONICAL_SLUG_COLLISION", null, null,
                    "One or more manifest slugs already belong to an unmapped content item"));
        }
        if (databaseState.legacyMappings() > 0) {
            issues.add(issue(WARNING, "BACKFILL_ALREADY_STARTED", null, null,
                    "Legacy mappings already exist; an apply operation must be idempotent"));
        }

        int creates = count(manifest.entries(), Action.CREATE);
        int merges = count(manifest.entries(), Action.MERGE_INTO);
        int skips = count(manifest.entries(), Action.SKIP_WITH_REASON);
        int blockers = (int) issues.stream().filter(item -> item.severity() == BLOCKER).count();
        int warnings = issues.size() - blockers;
        int projected = (int) manifest.entries().stream()
                .filter(item -> item.action() == Action.CREATE)
                .map(Entry::canonicalKey)
                .distinct()
                .count();

        return new BackfillReconciliationReport(
                manifest.schemaVersion(), manifest.primaryEntityId(), Instant.now(), true,
                blockers == 0,
                new BackfillReconciliationReport.Counts(sources.size(), manifest.entries().size(),
                        creates, merges, skips, projected, blockers, warnings),
                databaseState, List.copyOf(issues));
    }

    private void validateManifestHeader(BackfillManifest manifest, List<Issue> issues) {
        if (manifest.schemaVersion() != 1) {
            issues.add(issue(BLOCKER, "UNSUPPORTED_MANIFEST_VERSION", null, null,
                    "Only manifest schema version 1 is supported"));
        }
        if (manifest.primaryEntityId() == null) {
            issues.add(issue(BLOCKER, "MISSING_PRIMARY_ENTITY", null, null,
                    "The manifest must identify the PMO entity"));
        }
        if (manifest.entries() == null || manifest.entries().isEmpty()) {
            issues.add(issue(BLOCKER, "EMPTY_MANIFEST", null, null,
                    "The manifest contains no legacy sources"));
        }
    }

    private Map<String, Snapshot> indexSources(List<Snapshot> sources, List<Issue> issues) {
        Map<String, Snapshot> indexed = new LinkedHashMap<>();
        for (Snapshot source : sources) {
            if (indexed.putIfAbsent(source.sourceKey(), source) != null) {
                issues.add(issue(BLOCKER, "DUPLICATE_DISCOVERED_SOURCE", source.sourceKey(), null,
                        "The same live source key was discovered more than once"));
            }
        }
        return indexed;
    }

    private Map<String, Entry> indexEntries(List<Entry> entries, List<Issue> issues) {
        Map<String, Entry> indexed = new LinkedHashMap<>();
        if (entries == null) return indexed;
        for (Entry entry : entries) {
            if (indexed.putIfAbsent(entry.sourceKey(), entry) != null) {
                issues.add(issue(BLOCKER, "DUPLICATE_MANIFEST_SOURCE", entry.sourceKey(), entry.canonicalKey(),
                        "The same source key appears more than once in the manifest"));
            }
        }
        return indexed;
    }

    private void validateCoverage(Map<String, Snapshot> sources, Map<String, Entry> entries,
                                  List<Issue> issues) {
        sources.keySet().stream().filter(key -> !entries.containsKey(key)).forEach(key ->
                issues.add(issue(BLOCKER, "UNMAPPED_LIVE_SOURCE", key, null,
                        "A live legacy source has no manifest decision")));
        entries.keySet().stream().filter(key -> !sources.containsKey(key)).forEach(key ->
                issues.add(issue(BLOCKER, "MISSING_LIVE_SOURCE", key, entries.get(key).canonicalKey(),
                        "A manifest entry no longer exists in the live source")));
    }

    private void validateSnapshots(Map<String, Snapshot> sources, Map<String, Entry> entries,
                                   List<Issue> issues) {
        entries.forEach((key, entry) -> {
            Snapshot source = sources.get(key);
            if (source == null) return;
            compare(key, entry.canonicalKey(), "TYPE", entry.sourceType(), source.sourceType(), issues);
            compare(key, entry.canonicalKey(), "TITLE", entry.expectedTitle(), source.title(), issues);
            compare(key, entry.canonicalKey(), "STATUS", entry.expectedStatus(), source.status(), issues);
            compare(key, entry.canonicalKey(), "CATEGORY", entry.expectedCategory(), source.category(), issues);
            compare(key, entry.canonicalKey(), "BYLINE", entry.expectedByline(), source.byline(), issues);
            compare(key, entry.canonicalKey(), "DATE", entry.expectedDate(), source.sourceDate(), issues);
            compare(key, entry.canonicalKey(), "SUMMARY", entry.expectedSummary(), source.summary(), issues);
        });
    }

    private void compare(String sourceKey, String canonicalKey, String field, String expected,
                         String actual, List<Issue> issues) {
        if (!Objects.equals(expected, actual)) {
            issues.add(issue(BLOCKER, "SOURCE_DRIFT_" + field, sourceKey, canonicalKey,
                    "Live source no longer matches the reviewed manifest snapshot"));
        }
    }

    private void validateActions(List<Entry> entries, Map<String, Snapshot> sources, List<Issue> issues) {
        if (entries == null) return;
        Map<String, Entry> creates = new HashMap<>();
        Set<String> slugs = new HashSet<>();
        for (Entry entry : entries) {
            if (!CONTENT_TYPES.contains(entry.sourceType())) {
                issues.add(issue(BLOCKER, "UNSUPPORTED_CONTENT_TYPE", entry.sourceKey(), entry.canonicalKey(),
                        "The source type is not supported by unified content V1"));
            }
            if (!CONTENT_STATUSES.contains(entry.expectedStatus())) {
                issues.add(issue(BLOCKER, "UNSUPPORTED_CONTENT_STATUS", entry.sourceKey(), entry.canonicalKey(),
                        "The mapped publication state is not supported"));
            }
            if (categories.slugFor(entry.expectedCategory()).isEmpty()) {
                issues.add(issue(BLOCKER, "UNMAPPED_CATEGORY", entry.sourceKey(), entry.canonicalKey(),
                        "The category has no approved CONTENT_CATEGORY taxonomy slug"));
            }
            if (entry.action() == null) {
                issues.add(issue(BLOCKER, "MISSING_ACTION", entry.sourceKey(), entry.canonicalKey(),
                        "Every source requires an explicit action"));
                continue;
            }
            if (entry.action() == Action.CREATE) {
                if (blank(entry.canonicalKey()) || blank(entry.slug())) {
                    issues.add(issue(BLOCKER, "INVALID_CREATE", entry.sourceKey(), entry.canonicalKey(),
                            "CREATE requires canonicalKey and slug"));
                } else {
                    if (creates.putIfAbsent(entry.canonicalKey(), entry) != null) {
                        issues.add(issue(BLOCKER, "DUPLICATE_CANONICAL_KEY", entry.sourceKey(),
                                entry.canonicalKey(), "Only one CREATE may own a canonical key"));
                    }
                    String slugKey = entry.sourceType() + ":ar:" + entry.slug();
                    if (!slugs.add(slugKey)) {
                        issues.add(issue(BLOCKER, "DUPLICATE_MANIFEST_SLUG", entry.sourceKey(),
                                entry.canonicalKey(), "The manifest contains a duplicate canonical slug"));
                    }
                }
            }
            if (entry.action() == Action.SKIP_WITH_REASON && blank(entry.skipReason())) {
                issues.add(issue(BLOCKER, "SKIP_WITHOUT_REASON", entry.sourceKey(), entry.canonicalKey(),
                        "SKIP_WITH_REASON requires an explanation"));
            }
        }

        Map<String, List<Entry>> groups = new LinkedHashMap<>();
        entries.stream().filter(item -> item.action() != Action.SKIP_WITH_REASON)
                .forEach(item -> groups.computeIfAbsent(item.canonicalKey(), ignored -> new ArrayList<>()).add(item));
        groups.forEach((canonicalKey, group) -> {
            Entry owner = creates.get(canonicalKey);
            if (owner == null) {
                group.forEach(entry -> issues.add(issue(BLOCKER, "MERGE_TARGET_NOT_FOUND", entry.sourceKey(),
                        canonicalKey, "MERGE_INTO must reference a canonical key owned by CREATE")));
                return;
            }
            Set<String> statuses = group.stream().map(Entry::expectedStatus).collect(java.util.stream.Collectors.toSet());
            if (statuses.size() > 1) {
                issues.add(issue(BLOCKER, "MERGE_STATUS_CONFLICT", null, canonicalKey,
                        "Sources merged into one item have conflicting publication states"));
            }
            boolean suppliesBody = group.stream().map(Entry::sourceKey).map(sources::get)
                    .filter(Objects::nonNull).anyMatch(Snapshot::suppliesBody);
            if (!suppliesBody) {
                issues.add(issue(BLOCKER, "MISSING_BODY_SOURCE", owner.sourceKey(), canonicalKey,
                        "No reviewed source supplies body or summary content for the first revision"));
            }
        });
    }

    private DatabaseState inspectDatabase(List<Entry> entries) {
        int contentItems = countQuery("select count(*) from content_items");
        int mappings = countQuery("select count(*) from legacy_content_mappings");
        int orphans = countQuery("""
                select count(*) from legacy_content_mappings mapping
                left join content_items item on item.id = mapping.content_item_id
                where item.id is null
                """);
        int collisions = 0;
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry.action() != Action.CREATE || blank(entry.slug())) continue;
                collisions += jdbcTemplate.queryForObject("""
                        select count(*) from content_items item
                        where item.content_type = ? and item.locale = 'ar' and item.slug = ?
                          and not exists (
                            select 1 from legacy_content_mappings mapping
                            where mapping.content_item_id = item.id
                          )
                        """, Integer.class, entry.sourceType(), entry.slug());
            }
        }
        return new DatabaseState(contentItems, mappings, orphans, collisions);
    }

    private void validatePrimaryEntity(UUID entityId, List<Issue> issues) {
        if (entityId == null) return;
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from government_entities where id = ?", Integer.class, entityId);
        if (count == null || count == 0) {
            issues.add(issue(BLOCKER, "PRIMARY_ENTITY_NOT_FOUND", null, null,
                    "The manifest primary government entity does not exist"));
        }
    }

    private int countQuery(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    private int count(List<Entry> entries, Action action) {
        if (entries == null) return 0;
        return (int) entries.stream().filter(item -> item.action() == action).count();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private Issue issue(BackfillReconciliationReport.Severity severity, String code,
                        String sourceKey, String canonicalKey, String message) {
        return new Issue(severity, code, sourceKey, canonicalKey, message);
    }
}
