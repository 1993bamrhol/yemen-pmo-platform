package ye.gov.pmo.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceDetailItemRequest(
        @NotBlank @Size(max = 500) String title,
        @Size(max = 10000) String description) {
}
