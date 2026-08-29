package ye.gov.pmo.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
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
class EditorialVerificationIntegrationTest {

    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PUBLISHED_ID = UUID.fromString("5c200000-0000-0000-0000-000000000001");
    private static final UUID PUBLISHED_REVISION_ID = UUID.fromString("5c210000-0000-0000-0000-000000000001");
    private static final UUID DRAFT_ID = UUID.fromString("5c200000-0000-0000-0000-000000000002");
    private static final UUID DRAFT_REVISION_ID = UUID.fromString("5c210000-0000-0000-0000-000000000002");
    private static final long PUBLISHER_ID = 951L;
    private static final long EDITOR_ID = 952L;

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        insertUser(PUBLISHER_ID, "editorial-publisher-test", "editorial-publisher@test.gov.ye");
        insertUser(EDITOR_ID, "editorial-editor-test", "editorial-editor@test.gov.ye");
        assign(PUBLISHER_ID, "PUBLISHER");
        assign(EDITOR_ID, "EDITOR");

        Timestamp now = Timestamp.from(Instant.parse("2026-08-29T12:00:00Z"));
        insertItem(PUBLISHED_ID, "editorial-published-test", now);
        insertRevision(PUBLISHED_REVISION_ID, PUBLISHED_ID, "محتوى منشور غير متحقق", now);
        jdbc.update("""
                update content_items
                set status = 'PUBLISHED', current_revision_id = ?, published_revision_id = ?,
                    first_published_at = ?, last_published_at = ?
                where id = ?
                """, PUBLISHED_REVISION_ID, PUBLISHED_REVISION_ID, now, now, PUBLISHED_ID);

        insertItem(DRAFT_ID, "editorial-draft-test", now);
        insertRevision(DRAFT_REVISION_ID, DRAFT_ID, "مسودة غير منشورة", now);
        jdbc.update("update content_items set current_revision_id = ? where id = ?",
                DRAFT_REVISION_ID, DRAFT_ID);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("delete from audit_events where actor_user_id in (?, ?) or resource_id in (?, ?)",
                PUBLISHER_ID, EDITOR_ID, PUBLISHED_ID.toString(), DRAFT_ID.toString());
        jdbc.update("""
                update content_items
                set status = 'DRAFT', current_revision_id = null, published_revision_id = null,
                    editorial_verification_status = 'UNVERIFIED',
                    editorial_verified_revision_id = null, provenance_source_type = null,
                    provenance_source_reference = null, editorial_verified_at = null,
                    editorial_verified_by = null
                where id in (?, ?)
                """, PUBLISHED_ID, DRAFT_ID);
        jdbc.update("delete from content_items where id in (?, ?)", PUBLISHED_ID, DRAFT_ID);
        jdbc.update("delete from role_assignments where user_id in (?, ?)", PUBLISHER_ID, EDITOR_ID);
        jdbc.update("delete from users where id in (?, ?)", PUBLISHER_ID, EDITOR_ID);
    }

    @Test
    void publishedButUnverifiedContentIsNotOfficiallyVisible() throws Exception {
        mockMvc.perform(get("/api/v1/content/{id}", PUBLISHED_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/admin/content/{id}", PUBLISHED_ID)
                        .with(user("editorial-publisher-test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.editorialVerification.status").value("UNVERIFIED"))
                .andExpect(jsonPath("$.editorialVerification.verifiedRevisionId").doesNotExist());
    }

    @Test
    void verifiedContentRetainsRevisionBoundProvenanceAndBecomesPublic() throws Exception {
        mockMvc.perform(put("/api/v1/admin/content/{id}/editorial-verification", PUBLISHED_ID)
                        .with(user("editorial-publisher-test"))
                        .header("X-Correlation-ID", "editorial-verification-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status":"VERIFIED",
                                  "sourceType":"OFFICIAL_SOURCE_REFERENCE",
                                  "sourceReference":"https://example.gov.ye/content/editorial-test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editorialVerification.status").value("VERIFIED"))
                .andExpect(jsonPath("$.editorialVerification.verifiedRevisionId")
                        .value(PUBLISHED_REVISION_ID.toString()))
                .andExpect(jsonPath("$.editorialVerification.sourceType")
                        .value("OFFICIAL_SOURCE_REFERENCE"))
                .andExpect(jsonPath("$.editorialVerification.sourceReference")
                        .value("https://example.gov.ye/content/editorial-test"))
                .andExpect(jsonPath("$.editorialVerification.verifiedBy").value(PUBLISHER_ID));

        Map<String, Object> verification = jdbc.queryForMap("""
                select editorial_verification_status, editorial_verified_revision_id,
                       provenance_source_type, provenance_source_reference, editorial_verified_by
                from content_items where id = ?
                """, PUBLISHED_ID);
        assertEquals("VERIFIED", verification.get("EDITORIAL_VERIFICATION_STATUS"));
        assertEquals(PUBLISHED_REVISION_ID, verification.get("EDITORIAL_VERIFIED_REVISION_ID"));
        assertEquals("OFFICIAL_SOURCE_REFERENCE", verification.get("PROVENANCE_SOURCE_TYPE"));
        assertEquals("https://example.gov.ye/content/editorial-test",
                verification.get("PROVENANCE_SOURCE_REFERENCE"));
        assertEquals(PUBLISHER_ID, ((Number) verification.get("EDITORIAL_VERIFIED_BY")).longValue());

        mockMvc.perform(get("/api/v1/content/{id}", PUBLISHED_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("محتوى منشور غير متحقق"));

        Integer audits = jdbc.queryForObject("""
                select count(*) from audit_events
                where resource_id = ? and action = 'CONTENT_EDITORIAL_VERIFIED'
                  and actor_user_id = ? and outcome = 'SUCCESS'
                """, Integer.class, PUBLISHED_ID.toString(), PUBLISHER_ID);
        assertEquals(1, audits);
    }

    @Test
    void draftCannotBeVerifiedAndUsesUnifiedConflictContract() throws Exception {
        mockMvc.perform(put("/api/v1/admin/content/{id}/editorial-verification", DRAFT_ID)
                        .with(user("editorial-publisher-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"VERIFIED","sourceType":"OFFICIAL_MANUAL_ENTRY",
                                 "sourceReference":"test:draft"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void editorialVerificationRequiresExistingPublishAuthority() throws Exception {
        mockMvc.perform(put("/api/v1/admin/content/{id}/editorial-verification", PUBLISHED_ID)
                        .with(user("editorial-editor-test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"REJECTED"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private void insertItem(UUID id, String slug, Timestamp now) {
        jdbc.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, 'NEWS', ?, ?, 'ar', 'DRAFT', ?, ?, 0)
                """, id, PMO_ID, slug, now, now);
    }

    private void insertRevision(UUID revisionId, UUID itemId, String title, Timestamp now) {
        jdbc.update("""
                insert into content_revisions (
                    id, content_item_id, revision_number, title, body, change_note, created_at
                ) values (?, ?, 1, ?, '<p>نص الاختبار</p>', 'Editorial verification fixture', ?)
                """, revisionId, itemId, title, now);
    }

    private void insertUser(long id, String username, String email) {
        jdbc.update("""
                insert into users (id, username, email, password, enabled, created_at)
                values (?, ?, ?, 'not-used-by-mock-security', true, current_timestamp)
                """, id, username, email);
    }

    private void assign(long userId, String roleName) {
        Long roleId = jdbc.queryForObject("select id from roles where name = ?", Long.class, roleName);
        jdbc.update("""
                insert into role_assignments (
                    id, user_id, role_id, scope_type, government_entity_id, enabled, created_at
                ) values (?, ?, ?, 'ENTITY', ?, true, current_timestamp)
                """, UUID.randomUUID(), userId, roleId, PMO_ID);
    }
}
