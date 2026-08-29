package ye.gov.pmo.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import ye.gov.pmo.shared.web.ApiErrorResponse;

public final class ApiV1AuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final AuthenticationEntryPoint legacyEntryPoint =
            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

    public ApiV1AuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (!request.getRequestURI().startsWith("/api/v1/")) {
            legacyEntryPoint.commence(request, response, exception);
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(
                "UNAUTHORIZED",
                "المصادقة مطلوبة للوصول إلى هذا المورد.",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI()));
    }
}
