package ye.gov.pmo.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GovernmentEntityRequest(
        @NotBlank @Size(max = 50) String entityTypeCode,
        UUID parentEntityId,
        @NotBlank @Size(max = 255) String officialNameAr,
        @Size(max = 255) String officialNameEn,
        @Size(max = 150) String shortNameAr,
        @NotBlank @Size(max = 160)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
        @NotNull String status,
        @Size(max = 10000) String description,
        @Size(max = 10000) String mandate,
        @Size(max = 500) String websiteUrl,
        @Email @Size(max = 320) String officialEmail,
        @Size(max = 80) String officialPhone,
        @Size(max = 1000) String officialAddressAr,
        @Size(max = 1000) String officialSourceReference) {
}
