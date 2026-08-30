package ye.gov.pmo.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ye.gov.pmo.services.domain.ServiceSourceType;
import ye.gov.pmo.services.domain.ServiceVerificationStatus;

public record ServiceVerificationRequest(
        @NotNull ServiceVerificationStatus status,
        ServiceSourceType sourceType,
        @Size(max = 1000) String sourceReference) {
}
