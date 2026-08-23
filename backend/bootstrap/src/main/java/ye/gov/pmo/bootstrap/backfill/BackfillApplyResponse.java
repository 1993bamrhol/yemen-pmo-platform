package ye.gov.pmo.bootstrap.backfill;

import java.time.Instant;

public record BackfillApplyResponse(
        int manifestSchemaVersion,
        boolean executed,
        int createdContentItems,
        int existingContentItems,
        int createdMappings,
        int existingMappings,
        int skippedSources,
        Instant completedAt) {
}
