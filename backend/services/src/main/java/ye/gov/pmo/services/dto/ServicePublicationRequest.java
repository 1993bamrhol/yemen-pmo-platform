package ye.gov.pmo.services.dto;

import jakarta.validation.constraints.NotNull;
import ye.gov.pmo.services.domain.ServicePublicationAction;

public record ServicePublicationRequest(
        @NotNull ServicePublicationAction action) {
}
