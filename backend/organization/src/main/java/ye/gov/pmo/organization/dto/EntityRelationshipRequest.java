package ye.gov.pmo.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record EntityRelationshipRequest(
        @NotNull UUID targetEntityId,
        @NotBlank String relationshipType,
        LocalDate validFrom,
        LocalDate validTo) {
}
