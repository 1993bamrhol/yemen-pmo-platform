package ye.gov.pmo.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ye.gov.pmo.content.domain.ContentAction;

public record ContentTransitionRequest(
        @NotNull ContentAction action,
        @Size(max = 2000) String comment,
        boolean breakGlass) {
}
