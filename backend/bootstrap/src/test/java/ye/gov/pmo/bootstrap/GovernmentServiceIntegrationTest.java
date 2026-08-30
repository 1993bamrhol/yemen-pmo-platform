package ye.gov.pmo.bootstrap;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GovernmentServiceIntegrationTest {

    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID INACTIVE_ENTITY_ID =
            UUID.fromString("5c400000-0000-0000-0000-000000000001");
    private static final long OUTSIDER_USER_ID = 954L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        cleanFixtures();
        jdbc.update("""
                insert into government_entities (
                    id, entity_type_id, official_name_ar, slug, status, created_at, updated_at
                ) values (?, 2, ?, ?, 'INACTIVE', current_timestamp, current_timestamp)
                """, INACTIVE_ENTITY_ID, "جهة اختبار غير نشطة — ليست بيانات إنتاج",
                "phase5c4-test-inactive-entity");
        jdbc.update("""
                insert into users (id, username, email, password, enabled, created_at)
                values (?, 'phase5c4-service-outsider', 'phase5c4-outsider@test.invalid',
                        'not-used-by-mock-security', true, current_timestamp)
                """, OUTSIDER_USER_ID);
    }

    @AfterEach
    void tearDown() {
        cleanFixtures();
    }

    @Test
    void verifiedPublishedServiceSupportsCatalogDetailEntityFilterAndOrderedSections() throws Exception {
        String longArabicName = "خدمة " + "ا".repeat(248);
        Map<String, Object> body = serviceBody(longArabicName, "phase5c4-test-public", PMO_ID);
        body.put("officialNameEn", "Phase 5C.4 Test Service");
        body.put("summaryAr", "ملخص اختباري غير مخصص للإنتاج");
        body.put("descriptionAr", "وصف اختباري غير مخصص للإنتاج");
        body.put("feesAr", "بيان رسوم اختباري");
        body.put("processingTimeAr", "مدة اختبارية");
        body.put("eligibility", List.of(item("شرط أهلية اختباري", null)));
        body.put("requirements", List.of(
                item("متطلب اختباري أول", "تفصيل اختباري"),
                item("متطلب اختباري ثانٍ", null)));
        body.put("steps", List.of(
                item("خطوة اختبارية أولى", null),
                item("خطوة اختبارية ثانية", "تفصيل الخطوة")));
        body.put("channels", List.of(Map.of(
                "type", "ONLINE",
                "label", "قناة اختبارية",
                "actionUrl", "https://example.gov.ye/services/phase5c4-test",
                "instructions", "تعليمات اختبارية")));

        UUID id = createService(body);
        publish(id);
        verify(id, "OFFICIAL_SOURCE_REFERENCE",
                "https://example.gov.ye/services/phase5c4-test");

        mockMvc.perform(get("/api/v1/services").param("entityId", PMO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]", id).exists())
                .andExpect(jsonPath("$.items[?(@.id == '%s')].officialName", id).value(longArabicName))
                .andExpect(jsonPath("$.items[?(@.id == '%s')].channels[0]", id).value("ONLINE"));

        mockMvc.perform(get("/api/v1/entities/{entityId}/services", PMO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]", id).exists());

        mockMvc.perform(get("/api/v1/services/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalPath").value("/services/phase5c4-test-public"))
                .andExpect(jsonPath("$.ownerEntity.id").value(PMO_ID.toString()))
                .andExpect(jsonPath("$.requirements", hasSize(2)))
                .andExpect(jsonPath("$.requirements[0].order").value(1))
                .andExpect(jsonPath("$.steps[1].order").value(2))
                .andExpect(jsonPath("$.channels[0].actionUrl")
                        .value("https://example.gov.ye/services/phase5c4-test"))
                .andExpect(jsonPath("$.source.type").value("OFFICIAL_SOURCE_REFERENCE"));

        mockMvc.perform(get("/api/v1/services/by-slug/phase5c4-test-public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        Integer auditCount = jdbc.queryForObject("""
                select count(*) from audit_events
                where resource_id = ? and action = 'SERVICE_VERIFIED' and outcome = 'SUCCESS'
                """, Integer.class, id.toString());
        org.assertj.core.api.Assertions.assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void optionalSectionsRemainAbsentWithoutInventedFallbacks() throws Exception {
        UUID id = createService(serviceBody(
                "خدمة اختبار اختيارية الحقول", "phase5c4-test-sparse", PMO_ID));
        publish(id);
        verify(id, "OFFICIAL_MANUAL_ENTRY", "test-fixture:phase5c4-sparse");

        mockMvc.perform(get("/api/v1/services/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(nullValue()))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.eligibility", hasSize(0)))
                .andExpect(jsonPath("$.requirements", hasSize(0)))
                .andExpect(jsonPath("$.steps", hasSize(0)))
                .andExpect(jsonPath("$.channels", hasSize(0)))
                .andExpect(jsonPath("$.fees").value(nullValue()))
                .andExpect(jsonPath("$.processingTime").value(nullValue()));
    }

    @Test
    void draftsUnverifiedUpdatesRejectedRecordsAndInactiveOwnersStayHidden() throws Exception {
        Map<String, Object> activeBody = serviceBody(
                "خدمة اختبار دورة النشر", "phase5c4-test-visibility", PMO_ID);
        UUID id = createService(activeBody);

        expectPublicNotFound(id);
        publish(id);
        expectPublicNotFound(id);
        verify(id, "APPROVED_IMPORT", "test-fixture:phase5c4-import");
        mockMvc.perform(get("/api/v1/services/{id}", id)).andExpect(status().isOk());

        activeBody.put("summaryAr", "تعديل اختباري يعيد التحقق");
        updateService(id, activeBody).andExpect(jsonPath("$.verification.status").value("UNVERIFIED"));
        expectPublicNotFound(id);

        publish(id);
        reject(id);
        expectPublicNotFound(id);

        UUID inactiveOwnerService = createService(serviceBody(
                "خدمة اختبار لجهة غير نشطة", "phase5c4-test-inactive-owner", INACTIVE_ENTITY_ID));
        publish(inactiveOwnerService);
        verify(inactiveOwnerService, "OFFICIAL_MANUAL_ENTRY", "test-fixture:inactive-owner");
        expectPublicNotFound(inactiveOwnerService);
        mockMvc.perform(get("/api/v1/services").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(inactiveOwnerService.toString()))));
    }

    @Test
    void slugIsUniqueImmutableAndDoesNotDependOnNameChanges() throws Exception {
        Map<String, Object> body = serviceBody(
                "خدمة اختبار الرابط", "phase5c4-test-stable-slug", PMO_ID);
        UUID id = createService(body);

        mockMvc.perform(post("/api/v1/admin/services")
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        body.put("officialNameAr", "اسم اختباري جديد مع بقاء الرابط");
        updateService(id, body).andExpect(status().isOk())
                .andExpect(jsonPath("$.service.slug").value("phase5c4-test-stable-slug"));

        body.put("slug", "phase5c4-test-changed-slug");
        updateService(id, body).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void missingResourcesAndInvalidFiltersUseTheUnifiedErrorContract() throws Exception {
        UUID missingId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        mockMvc.perform(get("/api/v1/services/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/services/by-slug/not-a-real-service"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/services").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unsafeUrlsAndInvalidProvenanceAreRejected() throws Exception {
        Map<String, Object> unsafe = serviceBody(
                "خدمة اختبار رابط غير آمن", "phase5c4-test-unsafe-url", PMO_ID);
        unsafe.put("channels", List.of(Map.of(
                "type", "ONLINE", "actionUrl", "javascript:alert(1)")));

        mockMvc.perform(post("/api/v1/admin/services")
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(unsafe)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        UUID id = createService(serviceBody(
                "خدمة اختبار مصدر غير آمن", "phase5c4-test-unsafe-source", PMO_ID));
        publish(id);
        mockMvc.perform(put("/api/v1/admin/services/{id}/verification", id)
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"VERIFIED","sourceType":"OFFICIAL_SOURCE_REFERENCE",
                                 "sourceReference":"http://example.gov.ye/not-https"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void adminMutationsRequireAuthenticationAndScopedPermission() throws Exception {
        Map<String, Object> body = serviceBody(
                "خدمة اختبار الصلاحيات", "phase5c4-test-authorization", PMO_ID);

        mockMvc.perform(post("/api/v1/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/v1/admin/services")
                        .with(user("phase5c4-service-outsider"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private UUID createService(Map<String, Object> body) throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/services")
                        .with(user("admin"))
                        .header("X-Correlation-ID", "phase5c4-integration-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lifecycleStatus").value("DRAFT"))
                .andExpect(jsonPath("$.verification.status").value("UNVERIFIED"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.path("service").path("id").asText());
    }

    private void publish(UUID id) throws Exception {
        mockMvc.perform(put("/api/v1/admin/services/{id}/publication", id)
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"PUBLISH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycleStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.verification.status").value("UNVERIFIED"));
    }

    private void verify(UUID id, String sourceType, String sourceReference) throws Exception {
        mockMvc.perform(put("/api/v1/admin/services/{id}/verification", id)
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "status", "VERIFIED",
                                "sourceType", sourceType,
                                "sourceReference", sourceReference))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verification.status").value("VERIFIED"));
    }

    private void reject(UUID id) throws Exception {
        mockMvc.perform(put("/api/v1/admin/services/{id}/verification", id)
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verification.status").value("REJECTED"));
    }

    private org.springframework.test.web.servlet.ResultActions updateService(
            UUID id, Map<String, Object> body) throws Exception {
        return mockMvc.perform(put("/api/v1/admin/services/{id}", id)
                .with(user("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private void expectPublicNotFound(UUID id) throws Exception {
        mockMvc.perform(get("/api/v1/services/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private Map<String, Object> serviceBody(String name, String slug, UUID ownerId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("owningEntityId", ownerId.toString());
        body.put("slug", slug);
        body.put("officialNameAr", name);
        return body;
    }

    private Map<String, Object> item(String title, String description) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        if (description != null) item.put("description", description);
        return item;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void cleanFixtures() {
        jdbc.update("""
                delete from audit_events
                where resource_type = 'GovernmentService'
                   or actor_user_id = ?
                   or government_entity_id = ?
                """, OUTSIDER_USER_ID, INACTIVE_ENTITY_ID);
        jdbc.update("delete from government_services where slug like 'phase5c4-test-%'");
        jdbc.update("delete from government_entities where id = ?", INACTIVE_ENTITY_ID);
        jdbc.update("delete from users where id = ?", OUTSIDER_USER_ID);
    }
}
