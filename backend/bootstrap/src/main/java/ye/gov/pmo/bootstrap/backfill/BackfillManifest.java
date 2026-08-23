package ye.gov.pmo.bootstrap.backfill;

import java.util.List;
import java.util.UUID;

public record BackfillManifest(
        int schemaVersion,
        UUID primaryEntityId,
        List<Entry> entries) {

    public enum Action {
        CREATE,
        MERGE_INTO,
        SKIP_WITH_REASON
    }

    public record Entry(
            String sourceSystem,
            String sourceType,
            long legacyId,
            Action action,
            String canonicalKey,
            String slug,
            String skipReason,
            String expectedTitle,
            String expectedStatus,
            String expectedCategory,
            String expectedByline,
            String expectedDate,
            String expectedSummary) {

        public String sourceKey() {
            return sourceSystem + ":" + sourceType + ":" + legacyId;
        }
    }
}
