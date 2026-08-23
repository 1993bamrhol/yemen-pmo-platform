package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.service.RoleAssignmentService;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.service.UserService;
import ye.gov.pmo.shared.audit.AuditEventRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformFoundationIntegrationTest {

    private static final String PASSWORD = "TestOnlyAdminPassword!2026";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserService userService;
    @Autowired private RoleService roleService;
    @Autowired private RoleAssignmentService assignmentService;
    @Autowired private AuditEventRepository auditEventRepository;

    @Test
    void directoryAndScopedAdministrationAreIsolatedByEntity() throws Exception {
        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].canonicalPath").value("/prime-ministers-office"));

        String platformToken = login("admin", PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[?(@ == 'PLATFORM_SUPER_ADMIN')]").exists());
        UUID healthId = createMinistry(platformToken, "وزارة الصحة للاختبار", "health-test");
        UUID financeId = createMinistry(platformToken, "وزارة المالية للاختبار", "finance-test");

        mockMvc.perform(post("/api/v1/admin/entities/{id}/relationships", healthId)
                        .header("Authorization", bearer(platformToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "targetEntityId", financeId,
                                "relationshipType", "AFFILIATED_WITH"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceEntityId").value(healthId.toString()))
                .andExpect(jsonPath("$.targetEntityId").value(financeId.toString()));

        User entityAdmin = createUser("health-entity-admin", "health-entity-admin@example.com");
        Role entityAdminRole = roleService.findByName("ENTITY_ADMIN");
        assignmentService.grantEntityForBootstrap(
                entityAdmin.getId(), entityAdminRole.getId(), healthId, userService.findByUsername("admin").getId());
        String entityToken = login(entityAdmin.getUsername(), PASSWORD);

        mockMvc.perform(put("/api/v1/admin/entities/{id}", healthId)
                        .header("Authorization", bearer(entityToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entityBody("وزارة الصحة المعدلة", "health-test")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/entities/{id}", financeId)
                        .header("Authorization", bearer(entityToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entityBody("محاولة عابرة للجهات", "finance-test")))
                .andExpect(status().isForbidden());
        assertTrue(auditEventRepository.findAll().stream().anyMatch(event ->
                "ENTITY_ACCESS_DENIED".equals(event.getAction())
                        && financeId.equals(event.getGovernmentEntityId())
                        && event.getOutcome() == AuditOutcome.DENIED));

        mockMvc.perform(post("/api/v1/admin/entities/{id}/relationships", healthId)
                        .header("Authorization", bearer(entityToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "targetEntityId", financeId,
                                "relationshipType", "OVERSEEN_BY"))))
                .andExpect(status().isForbidden());

        User editor = createUser("health-editor", "health-editor@example.com");
        Role editorRole = roleService.findByName("EDITOR");
        String assignmentJson = mockMvc.perform(post("/api/v1/admin/entities/{id}/assignments", healthId)
                        .header("Authorization", bearer(entityToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", editor.getId(), "roleId", editorRole.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.governmentEntityId").value(healthId.toString()))
                .andReturn().getResponse().getContentAsString();
        UUID assignmentId = UUID.fromString(objectMapper.readTree(assignmentJson).get("id").asText());

        mockMvc.perform(get("/api/v1/admin/entities/{id}/assignments", financeId)
                        .header("Authorization", bearer(entityToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/entities/{entityId}/assignments/{assignmentId}",
                                healthId, assignmentId)
                        .header("Authorization", bearer(entityToken)))
                .andExpect(status().isNoContent());
    }

    private UUID createMinistry(String token, String name, String slug) throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/entities")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entityBody(name, slug)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.canonicalPath").value("/ministries/" + slug))
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(PASSWORD);
        return userService.save(user);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("token").asText();
    }

    private String entityBody(String name, String slug) throws Exception {
        return json(Map.of(
                "entityTypeCode", "MINISTRY",
                "officialNameAr", name,
                "slug", slug,
                "status", "ACTIVE"));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
