package ye.gov.pmo.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import ye.gov.pmo.content.domain.ContentType;

public record ContentCreateRequest(
        @NotNull ContentType contentType,
        @NotBlank @Size(max = 180)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String slug,
        @NotBlank @Size(max = 12) String locale,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String summary,
        @NotBlank @Size(max = 200000) String body,
        @Size(max = 255) String byline,
        Set<@Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String> categorySlugs) {
}
