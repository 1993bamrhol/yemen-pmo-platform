package ye.gov.pmo.bootstrap.backfill;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BackfillReconciliationReport(
        int manifestSchemaVersion,
        UUID primaryEntityId,
        Instant generatedAt,
        boolean dryRun,
        boolean readyToApply,
        Counts counts,
        DatabaseState databaseState,
        List<Issue> issues) {

    public record Counts(
            int discoveredSources,
            int manifestEntries,
            int createActions,
            int mergeActions,
            int skipActions,
            int projectedContentItems,
            int blockers,
            int warnings) {
    }

    public record DatabaseState(
            int contentItems,
            int legacyMappings,
            int orphanMappings,
            int canonicalSlugCollisions) {
    }

    public record Issue(
            Severity severity,
            String code,
            String sourceKey,
            String canonicalKey,
            String message) {
    }

    public enum Severity {
        BLOCKER,
        WARNING
    }
}
