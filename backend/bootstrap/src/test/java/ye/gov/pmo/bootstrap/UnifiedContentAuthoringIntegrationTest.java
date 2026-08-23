package ye.gov.pmo.bootstrap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UnifiedContentAuthoringIntegrationTest {
    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HEALTH_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final long EDITOR_ID = 901L;
    private static final long PUBLISHER_ID = 902L;
    private static final long OUTSIDER_ID = 903L;

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;
    private UUID contentId;

    @BeforeAll
    void setUpScopedActors() {
        jdbc.update("""
                insert into government_entities (
                    id, entity_type_id, official_name_ar, short_name_ar, slug, status,
                    created_at, updated_at
                ) values (?, 2, 'وزارة الصحة العامة والسكان', 'وزارة الصحة',
                    'health-authoring-test', 'ACTIVE', current_timestamp, current_timestamp)
                """, HEALTH_ID);
        insertUser(EDITOR_ID, "content-editor-test", "editor-content@test.gov.ye");
        insertUser(PUBLISHER_ID, "content-publisher-test", "publisher-content@test.gov.ye");
        insertUser(OUTSIDER_ID, "content-outsider-test", "outsider-content@test.gov.ye");
        assign(EDITOR_ID, "EDITOR", PMO_ID);
        assign(EDITOR_ID, "PUBLISHER", PMO_ID);
        assign(PUBLISHER_ID, "PUBLISHER", PMO_ID);
        assign(OUTSIDER_ID, "EDITOR", HEALTH_ID);
    }

    @AfterAll
    void cleanUpScopedFixtures() {
        jdbc.update("delete from audit_events where actor_user_id in (?, ?, ?) or government_entity_id = ?",
                EDITOR_ID, PUBLISHER_ID, OUTSIDER_ID, HEALTH_ID);
        jdbc.update("delete from audit_events where action like 'CONTENT_%'");
        jdbc.update("""
                update content_items set status = 'DRAFT', current_revision_id = null, published_revision_id = null
                where slug in ('slice-three-authoring', 'break-glass-authoring-test')
                """);
        jdbc.update("""
                delete from content_items
                where slug in ('slice-three-authoring', 'break-glass-authoring-test')
                """);
        jdbc.update("delete from role_assignments where user_id in (?, ?, ?)",
                EDITOR_ID, PUBLISHER_ID, OUTSIDER_ID);
        jdbc.update("delete from users where id in (?, ?, ?)", EDITOR_ID, PUBLISHER_ID, OUTSIDER_ID);
        jdbc.update("delete from government_entities where id = ?", HEALTH_ID);
    }

    @Test
    @Order(1)
    void entityEditorCreatesSanitizedDraftButCannotCrossEntityBoundary() throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/entities/{entityId}/content", PMO_ID)
                        .with(user("content-editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contentType":"NEWS",
                                  "slug":"slice-three-authoring",
                                  "locale":"ar",
                                  "title":"خبر دورة النشر",
                                  "summary":"ملخص",
                                  "body":"<h2>عنوان</h2><script>alert(1)</script><p>نص آمن</p>",
                                  "byline":"فريق التحرير",
                                  "categorySlugs":[]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currentRevision.number").value(1))
                .andExpect(jsonPath("$.currentRevision.body").value("<h2>عنوان</h2><p>نص آمن</p>"))
                .andReturn().getResponse().getContentAsString();
        contentId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(post("/api/v1/admin/entities/{entityId}/content", HEALTH_ID)
                        .with(user("content-editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"NEWS","slug":"cross-entity-denied","locale":"ar",
                                 "title":"غير مصرح","body":"<p>غير مصرح</p>","categorySlugs":[]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    void draftSupportsImmutableRevisionsAndEnforcesSeparationOfDuties() throws Exception {
        mockMvc.perform(post("/api/v1/admin/content/{id}/revisions", contentId)
                        .with(user("content-editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"خبر منقح","summary":"ملخص منقح","body":"<p>نسخة ثانية</p>",
                                 "byline":"فريق التحرير","changeNote":"مراجعة الصياغة"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentRevision.number").value(2));

        transition("content-editor-test", "SUBMIT_REVIEW", false, "جاهز للمراجعة", status().isOk());
        transition("content-editor-test", "APPROVE", false, "اعتماد ذاتي", status().isConflict());
    }

    @Test
    @Order(3)
    void separatePublisherCompletesWorkflowAndDraftNeverLeaks() throws Exception {
        transition("content-publisher-test", "APPROVE", false, "تمت المراجعة", status().isOk());

        mockMvc.perform(get("/api/v1/content/{id}", contentId))
                .andExpect(status().isNotFound());

        transition("content-publisher-test", "PUBLISH", false, "اعتماد النشر", status().isOk());

        mockMvc.perform(get("/api/v1/content/{id}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("خبر منقح"));

        mockMvc.perform(post("/api/v1/admin/content/{id}/revisions", contentId)
                        .with(user("content-editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"خبر محدث بعد النشر","summary":"تحديث جديد",
                                 "body":"<p>نسخة ثالثة</p>","byline":"فريق التحرير",
                                 "changeNote":"تحديث المحتوى المنشور"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.currentRevision.number").value(3))
                .andExpect(jsonPath("$.publishedRevision.number").value(2));

        transition("content-editor-test", "SUBMIT_REVIEW", false, "مراجعة تحديث منشور", status().isOk());
        mockMvc.perform(get("/api/v1/content/{id}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("خبر منقح"));
        transition("content-publisher-test", "APPROVE", false, "اعتماد التحديث", status().isOk());
        transition("content-publisher-test", "PUBLISH", false, "نشر التحديث", status().isOk());
        mockMvc.perform(get("/api/v1/content/{id}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("خبر محدث بعد النشر"));

        mockMvc.perform(get("/api/v1/admin/content/{id}", contentId)
                        .with(user("content-outsider-test")))
                .andExpect(status().isForbidden());

        transition("content-publisher-test", "PUBLISH", false, "نشر مكرر", status().isConflict());

        Integer transitionCount = jdbc.queryForObject(
                "select count(*) from content_transitions where content_item_id = ?", Integer.class, contentId);
        Integer failureAuditCount = jdbc.queryForObject("""
                select count(*) from audit_events
                where resource_id = ? and outcome = 'FAILURE'
                """, Integer.class, contentId.toString());
        org.junit.jupiter.api.Assertions.assertEquals(6, transitionCount);
        org.junit.jupiter.api.Assertions.assertTrue(
                failureAuditCount >= 2, "failureAuditCount=" + failureAuditCount);
    }

    @Test
    @Order(4)
    void platformAdministratorUsesExplicitAuditedBreakGlass() throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/entities/{entityId}/content", PMO_ID)
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"ANNOUNCEMENT","slug":"break-glass-authoring-test","locale":"ar",
                                 "title":"اختبار الطوارئ","body":"<p>محتوى طارئ</p>","categorySlugs":[]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID breakGlassContentId = UUID.fromString(objectMapper.readTree(response).get("id").asText());
        UUID original = contentId;
        contentId = breakGlassContentId;
        transition("admin", "SUBMIT_REVIEW", false, "طلب عاجل", status().isOk());
        transition("admin", "APPROVE", true, "تجاوز طارئ معتمد مركزيًا", status().isOk());
        transition("admin", "PUBLISH", true, "نشر طارئ معتمد مركزيًا", status().isOk());
        contentId = original;

        Integer breakGlassAudits = jdbc.queryForObject("""
                select count(*) from audit_events
                where resource_id = ? and outcome = 'SUCCESS' and metadata like '%breakGlass=true%'
                """, Integer.class, breakGlassContentId.toString());
        org.junit.jupiter.api.Assertions.assertEquals(2, breakGlassAudits);
    }

    private void transition(String username, String action, boolean breakGlass, String comment,
                            org.springframework.test.web.servlet.ResultMatcher expected) throws Exception {
        mockMvc.perform(post("/api/v1/admin/content/{id}/transitions", contentId)
                        .with(user(username))
                        .header("X-Correlation-ID", "slice-3-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransitionPayload(action, comment, breakGlass))))
                .andExpect(expected);
    }

    private void insertUser(long id, String username, String email) {
        jdbc.update("""
                insert into users (id, username, email, password, enabled, created_at)
                values (?, ?, ?, 'not-used-by-mock-security', true, current_timestamp)
                """, id, username, email);
    }

    private void assign(long userId, String roleName, UUID entityId) {
        Long roleId = jdbc.queryForObject("select id from roles where name = ?", Long.class, roleName);
        jdbc.update("""
                insert into role_assignments (
                    id, user_id, role_id, scope_type, government_entity_id, enabled, created_at
                ) values (?, ?, ?, 'ENTITY', ?, true, current_timestamp)
                """, UUID.randomUUID(), userId, roleId, entityId);
    }

    private record TransitionPayload(String action, String comment, boolean breakGlass) {}
}
