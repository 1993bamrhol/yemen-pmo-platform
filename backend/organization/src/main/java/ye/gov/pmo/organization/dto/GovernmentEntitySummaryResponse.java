package ye.gov.pmo.organization.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GovernmentEntitySummaryResponse(
        UUID id,
        String locale,
        EntityTypeResponse type,
        String officialName,
        String shortName,
        String slug,
        String canonicalPath,
        String description,
        OffsetDateTime updatedAt) {
}
