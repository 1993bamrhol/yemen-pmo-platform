package ye.gov.pmo.organization.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EntityRelationshipResponse(
        UUID id,
        UUID sourceEntityId,
        UUID targetEntityId,
        String relationshipType,
        LocalDate validFrom,
        LocalDate validTo,
        OffsetDateTime createdAt) {
}
