package ye.gov.pmo.identity.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RoleAssignmentRequest(
        @NotNull Long userId,
        @NotNull Long roleId,
        OffsetDateTime validFrom,
        OffsetDateTime validUntil) {
}
