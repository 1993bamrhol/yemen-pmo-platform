package ye.gov.pmo.bootstrap.shadow;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContentShadowComparisonReport(
        Instant generatedAt,
        boolean readOnly,
        boolean reconciliationReady,
        boolean readyForCanary,
        int legacyPublicSources,
        int mappedUnifiedItems,
        int differences,
        List<TypeComparison> contentTypes,
        HomeComparison portalHome) {

    public record TypeComparison(
            String contentType,
            String legacyListPath,
            int legacyCount,
            int mappedCount,
            int unifiedPublishedCount,
            int additionalUnifiedItems,
            boolean countParity,
            boolean orderParity,
            boolean fieldParity,
            boolean readyForCanary,
            List<ItemComparison> items,
            List<String> differences) {
    }

    public record ItemComparison(
            String sourceKey,
            long legacyId,
            UUID contentItemId,
            String legacyDetailPath,
            String canonicalPath,
            boolean mappingMatch,
            boolean titleMatch,
            boolean summaryMatch,
            boolean dateMatch,
            boolean categoryMatch,
            boolean canonicalPathMatch,
            List<String> differences) {
    }

    public record HomeComparison(
            int comparedSections,
            int matchedSections,
            boolean contentProjectionReady,
            List<SectionComparison> sections,
            List<String> excludedStaticSections) {
    }

    public record SectionComparison(
            String section,
            String sourceContentType,
            boolean titleAndOrderMatch) {
    }
}
