package ye.gov.pmo.bootstrap;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.shared.web.ApiV1;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiV1ErrorContractIntegrationTest.ErrorProbeController.class)
class ApiV1ErrorContractIntegrationTest {

    private static final String INTERNAL_DETAIL =
            "jdbc:postgresql://database/internal SQLSTATE secret";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "entities.manage")
    void malformedJsonUsesValidationContract() throws Exception {
        mockMvc.perform(post("/api/v1/admin/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/entities"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "entities.manage")
    void beanValidationReturnsSafeFieldDetails() throws Exception {
        mockMvc.perform(post("/api/v1/admin/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[*].field", containsInAnyOrder(
                        "entityTypeCode", "officialNameAr", "slug", "status")))
                .andExpect(jsonPath("$.details[*].reason", containsInAnyOrder(
                        "NOT_BLANK", "NOT_BLANK", "NOT_BLANK", "NOT_NULL")))
                .andExpect(content().string(not(containsString("rejectedValue"))));
    }

    @Test
    void unauthenticatedV1RequestUsesUnauthorizedContract() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content-compatibility/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/admin/content-compatibility/status"));
    }

    @Test
    @WithMockUser
    void authenticatedUserWithoutAuthorityUsesForbiddenContract() throws Exception {
        mockMvc.perform(post("/api/v1/admin/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entityTypeCode": "MINISTRY",
                                  "officialNameAr": "جهة اختبار",
                                  "slug": "test-entity",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void missingOrganizationResourceUsesNotFoundContract() throws Exception {
        UUID missingId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        mockMvc.perform(get("/api/v1/entities/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.details").doesNotExist())
                .andExpect(content().string(not(containsString("Government entity"))));
    }

    @Test
    @WithMockUser
    void identityResourceExceptionUsesV1NotFoundContract() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/identity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(content().string(not(containsString("private identity detail"))));
    }

    @Test
    void contentDomainBadRequestUsesSameContract() throws Exception {
        mockMvc.perform(get("/api/v1/content").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/content"))
                .andExpect(content().string(not(containsString("size must"))));
    }

    @Test
    @WithMockUser
    void unprocessableStatusHasStableCodeAndSafeMessage() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/unprocessable"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(content().string(not(containsString("private workflow state"))));
    }

    @Test
    @WithMockUser
    void annotatedDomainConflictKeepsItsStatusAndUsesSafeContract() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(content().string(not(containsString("private conflict detail"))));
    }

    @Test
    @WithMockUser
    void unexpectedExceptionDoesNotLeakInternalDetails() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/api/v1/test-errors/internal"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }

    @RestController
    @ApiV1
    @RequestMapping("/api/v1/test-errors")
    static class ErrorProbeController {

        @GetMapping("/unprocessable")
        String unprocessable() {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "private workflow state");
        }

        @GetMapping("/internal")
        String internal() {
            throw new IllegalStateException(INTERNAL_DETAIL);
        }

        @GetMapping("/identity-not-found")
        String identityNotFound() {
            throw new ResourceNotFoundException("private identity detail");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new TestConflictException();
        }
    }

    @ResponseStatus(org.springframework.http.HttpStatus.CONFLICT)
    static class TestConflictException extends RuntimeException {

        TestConflictException() {
            super("private conflict detail");
        }
    }
}
