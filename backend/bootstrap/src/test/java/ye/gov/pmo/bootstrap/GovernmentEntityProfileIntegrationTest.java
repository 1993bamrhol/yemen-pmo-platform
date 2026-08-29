package ye.gov.pmo.bootstrap;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", authorities = "entities.manage")
class GovernmentEntityProfileIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void publicDirectoryAndDetailExposeOnlyTheTypedOptionalProfileContract() throws Exception {
        String longArabicName = "وزارة " + "ا".repeat(240);
        Map<String, Object> profile = entityBody(longArabicName, "profile-contract-test", "ACTIVE");
        profile.put("officialNameEn", "Contract Test Ministry");
        profile.put("shortNameAr", "وزارة الاختبار");
        profile.put("description", "وصف رسمي اختباري");
        profile.put("mandate", "اختصاص رسمي اختباري");
        profile.put("websiteUrl", "https://example.gov.ye");
        profile.put("officialEmail", "contact@example.gov.ye");
        profile.put("officialPhone", "+967-1-000000");
        profile.put("officialAddressAr", "عنوان رسمي اختباري");
        profile.put("officialSourceReference", "official-registry:test-only");
        UUID profileId = createEntity(profile);
        UUID sparseId = createEntity(entityBody("جهة اختبار اختيارية الحقول", "sparse-profile-test", "ACTIVE"));

        mockMvc.perform(get("/api/v1/entity-directory")
                        .param("type", "MINISTRY")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.items[?(@.id == '%s')]", profileId).exists())
                .andExpect(jsonPath("$.items[?(@.id == '%s')].locale", profileId).value("ar"))
                .andExpect(jsonPath("$.items[?(@.id == '%s')].canonicalPath", profileId)
                        .value("/ministries/profile-contract-test"));

        mockMvc.perform(get("/api/v1/entities/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officialName").value(longArabicName))
                .andExpect(jsonPath("$.officialNameEn").value("Contract Test Ministry"))
                .andExpect(jsonPath("$.mandate").value("اختصاص رسمي اختباري"))
                .andExpect(jsonPath("$.contact.email").value("contact@example.gov.ye"))
                .andExpect(jsonPath("$.contact.phone").value("+967-1-000000"))
                .andExpect(jsonPath("$.contact.address").value("عنوان رسمي اختباري"))
                .andExpect(jsonPath("$.officialSourceReference").value("official-registry:test-only"));

        mockMvc.perform(get("/api/v1/entities/{id}", sparseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officialNameEn").value(nullValue()))
                .andExpect(jsonPath("$.mandate").value(nullValue()))
                .andExpect(jsonPath("$.contact").value(nullValue()))
                .andExpect(jsonPath("$.officialSourceReference").value(nullValue()));

        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == '%s')]", profileId).exists());
    }

    @Test
    void inactiveEntitiesRemainInvisibleAcrossEveryPublicRead() throws Exception {
        UUID inactiveId = createEntity(entityBody(
                "جهة اختبار غير نشطة", "inactive-profile-test", "INACTIVE"));

        mockMvc.perform(get("/api/v1/entities/{id}", inactiveId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/entities/by-slug/ministries/inactive-profile-test"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/entity-directory").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(inactiveId.toString()))));
        mockMvc.perform(get("/api/v1/entities"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(inactiveId.toString()))));
    }

    @Test
    void slugChangesKeepOneLevelOfDurableAliasesWithoutAllowingReuse() throws Exception {
        UUID entityId = createEntity(entityBody(
                "جهة اختبار تغيير الرابط", "old-profile-slug", "ACTIVE"));

        mockMvc.perform(put("/api/v1/admin/entities/{id}", entityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(entityBody(
                                "جهة اختبار تغيير الرابط", "new-profile-slug", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/ministries/new-profile-slug"));

        mockMvc.perform(get("/api/v1/entities/by-slug/ministries/old-profile-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entityId.toString()))
                .andExpect(jsonPath("$.slug").value("new-profile-slug"))
                .andExpect(jsonPath("$.canonicalPath").value("/ministries/new-profile-slug"));

        Integer aliases = jdbcTemplate.queryForObject("""
                select count(*) from government_entity_slug_aliases
                where government_entity_id = ? and public_path_segment = ? and slug = ?
                """, Integer.class, entityId, "ministries", "old-profile-slug");
        org.assertj.core.api.Assertions.assertThat(aliases).isEqualTo(1);

        mockMvc.perform(post("/api/v1/admin/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(entityBody(
                                "جهة تحاول إعادة استخدام الرابط", "old-profile-slug", "ACTIVE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(put("/api/v1/admin/entities/{id}", entityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(entityBody(
                                "جهة اختبار تغيير الرابط", "old-profile-slug", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/ministries/old-profile-slug"));
        mockMvc.perform(get("/api/v1/entities/by-slug/ministries/new-profile-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entityId.toString()))
                .andExpect(jsonPath("$.canonicalPath").value("/ministries/old-profile-slug"));
    }

    @Test
    void invalidDirectoryRequestsAndMissingEntitiesUseTheUnifiedErrorContract() throws Exception {
        UUID missingId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        mockMvc.perform(get("/api/v1/entities/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/v1/entity-directory").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/v1/entity-directory").param("type", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private UUID createEntity(Map<String, Object> body) throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private Map<String, Object> entityBody(String name, String slug, String status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("entityTypeCode", "MINISTRY");
        body.put("officialNameAr", name);
        body.put("slug", slug);
        body.put("status", status);
        return body;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
