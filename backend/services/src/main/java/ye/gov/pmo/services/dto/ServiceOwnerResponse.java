package ye.gov.pmo.services.dto;

import java.util.UUID;

public record ServiceOwnerResponse(
        UUID id,
        String officialName,
        String canonicalPath) {
}
