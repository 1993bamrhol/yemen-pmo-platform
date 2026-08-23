package ye.gov.pmo.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoleAssignmentResponse(
        UUID id,
        Long userId,
        String username,
        Long roleId,
        String role,
        String scopeType,
        UUID governmentEntityId,
        OffsetDateTime validFrom,
        OffsetDateTime validUntil,
        boolean enabled,
        OffsetDateTime createdAt) {
}
