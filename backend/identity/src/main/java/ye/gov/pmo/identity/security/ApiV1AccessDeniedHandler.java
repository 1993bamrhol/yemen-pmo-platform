package ye.gov.pmo.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import ye.gov.pmo.shared.web.ApiErrorResponse;

public final class ApiV1AccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AccessDeniedHandler legacyAccessDeniedHandler = new AccessDeniedHandlerImpl();

    public ApiV1AccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException, ServletException {
        if (!request.getRequestURI().startsWith("/api/v1/")) {
            legacyAccessDeniedHandler.handle(request, response, exception);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(
                "FORBIDDEN",
                "لا تملك صلاحية الوصول إلى هذا المورد.",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()));
    }
}
