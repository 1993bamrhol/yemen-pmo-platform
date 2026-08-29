package ye.gov.pmo.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp,
        List<ApiFieldError> details) {

    public ApiErrorResponse {
        details = details == null ? null : List.copyOf(details);
    }

    public static ApiErrorResponse of(String code, String message, int status, String path) {
        return new ApiErrorResponse(code, message, status, path, Instant.now(), null);
    }

    public static ApiErrorResponse validation(
            String message, int status, String path, List<ApiFieldError> details) {
        return new ApiErrorResponse(
                "VALIDATION_ERROR", message, status, path, Instant.now(), details);
    }
}
