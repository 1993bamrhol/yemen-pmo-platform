package ye.gov.pmo.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ye.gov.pmo.content.domain.EditorialSourceType;
import ye.gov.pmo.content.domain.EditorialVerificationStatus;

public record EditorialVerificationRequest(
        @NotNull EditorialVerificationStatus status,
        EditorialSourceType sourceType,
        @Size(max = 1000) String sourceReference) {
}
