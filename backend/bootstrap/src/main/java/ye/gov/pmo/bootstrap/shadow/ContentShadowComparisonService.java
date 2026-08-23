package ye.gov.pmo.bootstrap.shadow;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillReconciliationService;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog.Snapshot;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport.HomeComparison;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport.ItemComparison;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport.SectionComparison;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport.TypeComparison;
import ye.gov.pmo.content.dto.PublicContentResponse;
import ye.gov.pmo.content.service.PublicContentService;

@Service
public class ContentShadowComparisonService {
    private static final List<TypeDefinition> TYPES = List.of(
            new TypeDefinition("NEWS", "STATIC_NEWS", "/api/news", "/news/"),
            new TypeDefinition("ANNOUNCEMENT", "STATIC_ANNOUNCEMENTS", "/api/announcements", "/announcements/"),
            new TypeDefinition("DECISION", "STATIC_DECISIONS", "/api/decisions", "/decisions/"),
            new TypeDefinition("DOCUMENT", "STATIC_DOCUMENTS", "/api/documents", "/documents/"));

    private static final Map<String, Integer> ARABIC_MONTHS = Map.ofEntries(
            Map.entry("يناير", 1), Map.entry("فبراير", 2), Map.entry("مارس", 3),
            Map.entry("أبريل", 4), Map.entry("مايو", 5), Map.entry("يونيو", 6),
            Map.entry("يوليو", 7), Map.entry("أغسطس", 8), Map.entry("سبتمبر", 9),
            Map.entry("أكتوبر", 10), Map.entry("نوفمبر", 11), Map.entry("ديسمبر", 12));

    private final LegacyContentSourceCatalog legacyCatalog;
    private final ContentBackfillReconciliationService reconciliation;
    private final PublicContentService publicContent;
    private final JdbcTemplate jdbcTemplate;

    public ContentShadowComparisonService(LegacyContentSourceCatalog legacyCatalog,
                                          ContentBackfillReconciliationService reconciliation,
                                          PublicContentService publicContent,
                                          JdbcTemplate jdbcTemplate) {
        this.legacyCatalog = legacyCatalog;
        this.reconciliation = reconciliation;
        this.publicContent = publicContent;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public ContentShadowComparisonReport compare() {
        boolean reconciliationReady = reconciliation.reconcile().readyToApply();
        List<Snapshot> allSources = legacyCatalog.discover();
        List<TypeComparison> comparisons = TYPES.stream()
                .map(type -> compareType(type, allSources))
                .toList();
        int legacyCount = comparisons.stream().mapToInt(TypeComparison::legacyCount).sum();
        int mappedCount = comparisons.stream().mapToInt(TypeComparison::mappedCount).sum();
        int differences = comparisons.stream().mapToInt(item -> item.differences().size()
                + item.items().stream().mapToInt(detail -> detail.differences().size()).sum()).sum();
        boolean ready = reconciliationReady && comparisons.stream().allMatch(TypeComparison::readyForCanary);
        return new ContentShadowComparisonReport(
                java.time.Instant.now(), true, reconciliationReady, ready,
                legacyCount, mappedCount, differences, comparisons, compareHome(comparisons));
    }

    private TypeComparison compareType(TypeDefinition definition, List<Snapshot> allSources) {
        List<Snapshot> legacy = allSources.stream()
                .filter(source -> source.sourceSystem().equals(definition.sourceSystem()))
                .sorted(java.util.Comparator.comparingLong(Snapshot::legacyId))
                .toList();
        List<PublicContentResponse> unified = publicContent.findPublished(
                definition.contentType(), null, null, null, null, 0, 100).items();
        Map<UUID, PublicContentResponse> unifiedById = unified.stream()
                .collect(Collectors.toMap(PublicContentResponse::id, Function.identity()));

        List<ItemComparison> items = new ArrayList<>();
        List<UUID> expectedOrder = new ArrayList<>();
        for (Snapshot source : legacy) {
            UUID mappedId = findMappedId(source);
            if (mappedId != null) expectedOrder.add(mappedId);
            items.add(compareItem(definition, source, mappedId, unifiedById.get(mappedId)));
        }

        Set<UUID> mappedIds = items.stream().map(ItemComparison::contentItemId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        List<UUID> actualMappedOrder = unified.stream().map(PublicContentResponse::id)
                .filter(mappedIds::contains).toList();
        int additional = (int) unified.stream().map(PublicContentResponse::id)
                .filter(id -> !mappedIds.contains(id)).count();
        int mappedCount = mappedIds.size();
        boolean countParity = mappedCount == legacy.size() && additional == 0;
        boolean orderParity = expectedOrder.equals(actualMappedOrder);
        boolean fieldParity = items.stream().allMatch(item -> item.differences().isEmpty());
        List<String> differences = new ArrayList<>();
        if (mappedCount != legacy.size()) differences.add("MAPPED_COUNT_MISMATCH");
        if (additional > 0) differences.add("ADDITIONAL_UNIFIED_ITEMS");
        if (!orderParity) differences.add("LIST_ORDER_MISMATCH");
        if (!fieldParity) differences.add("ITEM_FIELD_MISMATCH");

        return new TypeComparison(definition.contentType(), definition.legacyListPath(),
                legacy.size(), mappedCount, unified.size(), additional, countParity, orderParity,
                fieldParity, countParity && orderParity && fieldParity,
                List.copyOf(items), List.copyOf(differences));
    }

    private ItemComparison compareItem(TypeDefinition definition, Snapshot source, UUID mappedId,
                                       PublicContentResponse unified) {
        List<String> differences = new ArrayList<>();
        boolean mappingMatch = mappedId != null && unified != null;
        boolean titleMatch = mappingMatch && source.title().equals(unified.title());
        boolean summaryMatch = mappingMatch && Objects.equals(source.summary(), unified.summary());
        boolean dateMatch = mappingMatch && parseDate(source.sourceDate()).equals(publicationDate(unified.publishedAt()));
        boolean categoryMatch = mappingMatch && unified.categories().stream()
                .anyMatch(category -> source.category().equals(category.label()));
        String expectedCanonicalPath = unified == null ? null : definition.canonicalPrefix() + unified.slug();
        boolean pathMatch = mappingMatch && expectedCanonicalPath.equals(unified.canonicalPath());
        if (!mappingMatch) differences.add("MAPPING_OR_PUBLIC_ITEM_MISSING");
        if (mappingMatch && !titleMatch) differences.add("TITLE_MISMATCH");
        if (mappingMatch && !summaryMatch) differences.add("SUMMARY_MISMATCH");
        if (mappingMatch && !dateMatch) differences.add("DATE_MISMATCH");
        if (mappingMatch && !categoryMatch) differences.add("CATEGORY_MISMATCH");
        if (mappingMatch && !pathMatch) differences.add("CANONICAL_PATH_MISMATCH");

        return new ItemComparison(source.sourceKey(), source.legacyId(), mappedId,
                definition.legacyListPath() + "/" + source.legacyId(),
                unified == null ? null : unified.canonicalPath(), mappingMatch, titleMatch,
                summaryMatch, dateMatch, categoryMatch, pathMatch, List.copyOf(differences));
    }

    private HomeComparison compareHome(List<TypeComparison> comparisons) {
        Map<String, TypeComparison> byType = comparisons.stream()
                .collect(Collectors.toMap(TypeComparison::contentType, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));
        List<SectionComparison> sections = List.of(
                section("latestNews", "NEWS", byType),
                section("decisions", "DECISION", byType),
                section("documents", "DOCUMENT", byType));
        int matched = (int) sections.stream().filter(SectionComparison::titleAndOrderMatch).count();
        return new HomeComparison(sections.size(), matched, matched == sections.size(), sections,
                List.of("hero", "stats", "portalHighlights", "officialChannels", "officialStatements",
                        "serviceCards", "services", "mediaItems", "governancePrinciples"));
    }

    private SectionComparison section(String name, String type, Map<String, TypeComparison> comparisons) {
        TypeComparison comparison = comparisons.get(type);
        return new SectionComparison(name, type,
                comparison != null && comparison.fieldParity() && comparison.orderParity());
    }

    private UUID findMappedId(Snapshot source) {
        List<UUID> ids = jdbcTemplate.query("""
                select content_item_id from legacy_content_mappings
                where source_system = ? and source_type = ? and legacy_id = ?
                """, (resultSet, row) -> resultSet.getObject(1, UUID.class),
                source.sourceSystem(), source.sourceType(), source.legacyId());
        return ids.size() == 1 ? ids.getFirst() : null;
    }

    private LocalDate publicationDate(OffsetDateTime value) {
        return value.toInstant().atOffset(java.time.ZoneOffset.ofHours(3)).toLocalDate();
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException ignored) {
            String[] parts = value.split(" ");
            if (parts.length != 3 || !ARABIC_MONTHS.containsKey(parts[1])) {
                throw new IllegalStateException("Unsupported legacy date: " + value);
            }
            return LocalDate.of(Integer.parseInt(parts[2]), ARABIC_MONTHS.get(parts[1]),
                    Integer.parseInt(parts[0]));
        }
    }

    private record TypeDefinition(
            String contentType,
            String sourceSystem,
            String legacyListPath,
            String canonicalPrefix) {
    }
}
