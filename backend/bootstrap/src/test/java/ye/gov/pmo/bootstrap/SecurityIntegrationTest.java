package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ye.gov.pmo.identity.dto.AuthResponse;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    private static final String FRONTEND_ORIGIN = "http://localhost:3000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void corsAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN));
    }

    @Test
    void malformedJwtIsRejectedAsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void initialPmoAdministratorCanAccessProtectedContentAndSupportInbox() throws Exception {
        String token = loginAsInitialAdministrator();

        mockMvc.perform(get("/api/admin/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/support/requests")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void supportRequestValidationRejectsInvalidPublicInput() throws Exception {
        mockMvc.perform(post("/api/support/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "email": "not-an-email",
                                  "category": "unknown",
                                  "subject": "",
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private String loginAsInitialAdministrator() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "TestOnlyAdminPassword!2026"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AuthResponse.class).getToken();
    }
}
