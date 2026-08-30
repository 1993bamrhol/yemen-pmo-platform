package ye.gov.pmo.services.dto;

import java.time.OffsetDateTime;
import ye.gov.pmo.services.domain.ServiceSourceType;

public record ServiceSourceResponse(
        ServiceSourceType type,
        String reference,
        OffsetDateTime verifiedAt) {
}
