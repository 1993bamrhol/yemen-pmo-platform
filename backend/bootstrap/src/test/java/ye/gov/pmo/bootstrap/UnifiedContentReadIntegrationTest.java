package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnifiedContentReadIntegrationTest {

    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PUBLISHED_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PUBLISHED_REVISION_ID = UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID DRAFT_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID CATEGORY_ID = UUID.fromString("12000000-0000-0000-0000-000000000001");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void insertFixtures() {
        Timestamp publishedAt = Timestamp.from(Instant.parse("2026-08-20T09:00:00Z"));
        jdbcTemplate.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, 'NEWS', ?, 'unified-public-news', 'ar', 'DRAFT', ?, ?, 0)
                """, PUBLISHED_ID, PMO_ID, publishedAt, publishedAt);
        jdbcTemplate.update("""
                insert into content_revisions (
                    id, content_item_id, revision_number, title, summary, body,
                    byline, change_note, created_at
                ) values (?, ?, 1, ?, ?, ?, ?, ?, ?)
                """, PUBLISHED_REVISION_ID, PUBLISHED_ID,
                "خبر موحد منشور", "ملخص الخبر الموحد", "<p>النص الرسمي المنشور.</p>",
                "المركز الإعلامي", "Initial public revision", publishedAt);
        jdbcTemplate.update("""
                update content_items
                set status = 'PUBLISHED', current_revision_id = ?, published_revision_id = ?,
                    first_published_at = ?, last_published_at = ?, updated_at = ?,
                    editorial_verification_status = 'VERIFIED',
                    editorial_verified_revision_id = ?,
                    provenance_source_type = 'OFFICIAL_MANUAL_ENTRY',
                    provenance_source_reference = 'test:unified-public-news',
                    editorial_verified_at = ?,
                    editorial_verified_by = (select id from users where username = 'admin')
                where id = ?
                """, PUBLISHED_REVISION_ID, PUBLISHED_REVISION_ID,
                publishedAt, publishedAt, publishedAt, PUBLISHED_REVISION_ID,
                publishedAt, PUBLISHED_ID);
        jdbcTemplate.update("""
                insert into taxonomy_terms (
                    id, taxonomy_code, slug, label_ar, active, created_at
                ) values (?, 'CONTENT_CATEGORY', 'digital-government', 'الحكومة الرقمية', true, ?)
                """, CATEGORY_ID, publishedAt);
        jdbcTemplate.update("""
                insert into content_taxonomy_assignments (
                    content_item_id, taxonomy_term_id, created_at
                ) values (?, ?, ?)
                """, PUBLISHED_ID, CATEGORY_ID, publishedAt);
        jdbcTemplate.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, 'NEWS', ?, 'private-draft', 'ar', 'DRAFT', ?, ?, 0)
                """, DRAFT_ID, PMO_ID, publishedAt, publishedAt);
    }

    @Test
    void publicApiReturnsOnlyPublishedRevisionWithCanonicalMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(PUBLISHED_ID.toString()))
                .andExpect(jsonPath("$.items[0].title").value("خبر موحد منشور"))
                .andExpect(jsonPath("$.items[0].canonicalPath").value("/news/unified-public-news"))
                .andExpect(jsonPath("$.items[0].primaryEntity.id").value(PMO_ID.toString()))
                .andExpect(jsonPath("$.items[0].categories[0].slug").value("digital-government"));

        mockMvc.perform(get("/api/v1/content/{id}", PUBLISHED_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("<p>النص الرسمي المنشور.</p>"));

        mockMvc.perform(get("/api/v1/content/{id}", DRAFT_ID))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/content/by-slug/news/unified-public-news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PUBLISHED_ID.toString()));
    }

    @Test
    void publicFiltersAndEntityProjectionAreBoundedAndPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/content")
                        .param("type", "news")
                        .param("entityId", PMO_ID.toString())
                        .param("category", "digital-government")
                        .param("dateFrom", "2026-08-20")
                        .param("dateTo", "2026-08-20")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        mockMvc.perform(get("/api/v1/entities/{entityId}/content", PMO_ID)
                        .param("type", "DECISION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/v1/content").param("size", "101"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/content")
                        .param("dateFrom", "2026-08-21")
                        .param("dateTo", "2026-08-20"))
                .andExpect(status().isBadRequest());
    }
}
