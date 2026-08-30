package ye.gov.pmo.services.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record GovernmentServiceRequest(
        @NotNull UUID owningEntityId,
        @NotBlank @Size(max = 160)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
        @NotBlank @Size(max = 255) String officialNameAr,
        @Size(max = 255) String officialNameEn,
        @Size(max = 1000) String summaryAr,
        @Size(max = 20000) String descriptionAr,
        @Size(max = 2000) String feesAr,
        @Size(max = 1000) String processingTimeAr,
        List<@Valid ServiceDetailItemRequest> eligibility,
        List<@Valid ServiceDetailItemRequest> requirements,
        List<@Valid ServiceDetailItemRequest> steps,
        List<@Valid ServiceChannelRequest> channels) {
}
