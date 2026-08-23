package ye.gov.pmo.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentRevisionRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String summary,
        @NotBlank @Size(max = 200000) String body,
        @Size(max = 255) String byline,
        @NotBlank @Size(max = 1000) String changeNote) {
}
