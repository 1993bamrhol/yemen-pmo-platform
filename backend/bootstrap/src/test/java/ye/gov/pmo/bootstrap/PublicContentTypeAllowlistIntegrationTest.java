package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "features.unified-content-read.enabled=true",
        "features.unified-content-read.allowed-types=NEWS"
})
@AutoConfigureMockMvc
@Transactional
class PublicContentTypeAllowlistIntegrationTest {

    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Fixture OLDER_NEWS = fixture(
            "21000000-0000-0000-0000-000000000001", "NEWS", "allowed-news-older",
            "خبر مسموح أقدم", "2026-08-20T09:00:00Z", true);
    private static final Fixture NEWER_NEWS = fixture(
            "21000000-0000-0000-0000-000000000002", "NEWS", "allowed-news-newer",
            "خبر مسموح أحدث", "2026-08-22T09:00:00Z", true);
    private static final Fixture UNVERIFIED_NEWS = fixture(
            "21000000-0000-0000-0000-000000000003", "NEWS", "unverified-news",
            "خبر غير متحقق", "2026-08-26T09:00:00Z", false);
    private static final Fixture ANNOUNCEMENT = fixture(
            "22000000-0000-0000-0000-000000000001", "ANNOUNCEMENT", "blocked-announcement",
            "إعلان محجوب", "2026-08-23T09:00:00Z", true);
    private static final Fixture DECISION = fixture(
            "23000000-0000-0000-0000-000000000001", "DECISION", "blocked-decision",
            "قرار محجوب", "2026-08-24T09:00:00Z", true);
    private static final Fixture DOCUMENT = fixture(
            "24000000-0000-0000-0000-000000000001", "DOCUMENT", "blocked-document",
            "وثيقة محجوبة", "2026-08-25T09:00:00Z", true);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void insertFixtures() {
        List.of(OLDER_NEWS, NEWER_NEWS, UNVERIFIED_NEWS, ANNOUNCEMENT, DECISION, DOCUMENT)
                .forEach(this::insertFixture);
    }

    @Test
    void unfilteredAndEntityListsFilterBeforeOrderingCountsAndPagination() throws Exception {
        mockMvc.perform(get("/api/v1/content").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].id").value(NEWER_NEWS.id().toString()));

        mockMvc.perform(get("/api/v1/content").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items[0].id").value(OLDER_NEWS.id().toString()));

        mockMvc.perform(get("/api/v1/content").param("type", "news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/entities/{entityId}/content", PMO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void blockedTypesCannotBeReachedByFilterUuidSlugOrEntityEndpoint() throws Exception {
        for (Fixture blocked : List.of(ANNOUNCEMENT, DECISION, DOCUMENT)) {
            mockMvc.perform(get("/api/v1/content").param("type", blocked.type()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
            mockMvc.perform(get("/api/v1/content/{id}", blocked.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
            mockMvc.perform(get("/api/v1/content/by-slug/{type}/{slug}",
                            blocked.type().toLowerCase(Locale.ROOT), blocked.slug()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
            mockMvc.perform(get("/api/v1/entities/{entityId}/content", PMO_ID)
                            .param("type", blocked.type()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Test
    void allowedTypeStillRequiresPublishedVerifiedCurrentRevision() throws Exception {
        mockMvc.perform(get("/api/v1/content/{id}", NEWER_NEWS.id()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/content/by-slug/news/{slug}", NEWER_NEWS.slug()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/content/{id}", UNVERIFIED_NEWS.id()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/content/by-slug/news/{slug}", UNVERIFIED_NEWS.slug()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/content").param("type", "not-a-content-type"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private void insertFixture(Fixture fixture) {
        Timestamp publishedAt = Timestamp.from(fixture.publishedAt());
        jdbc.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, ?, ?, ?, 'ar', 'DRAFT', ?, ?, 0)
                """, fixture.id(), fixture.type(), PMO_ID, fixture.slug(), publishedAt, publishedAt);
        jdbc.update("""
                insert into content_revisions (
                    id, content_item_id, revision_number, title, body, change_note, created_at
                ) values (?, ?, 1, ?, '<p>نص اختبار allowlist.</p>', 'Allowlist test fixture', ?)
                """, fixture.revisionId(), fixture.id(), fixture.title(), publishedAt);
        if (fixture.verified()) {
            jdbc.update("""
                    update content_items
                    set status = 'PUBLISHED', current_revision_id = ?, published_revision_id = ?,
                        first_published_at = ?, last_published_at = ?, updated_at = ?,
                        editorial_verification_status = 'VERIFIED',
                        editorial_verified_revision_id = ?,
                        provenance_source_type = 'OFFICIAL_MANUAL_ENTRY',
                        provenance_source_reference = ?, editorial_verified_at = ?,
                        editorial_verified_by = (select id from users where username = 'admin')
                    where id = ?
                    """, fixture.revisionId(), fixture.revisionId(), publishedAt, publishedAt,
                    publishedAt, fixture.revisionId(), "test:" + fixture.slug(), publishedAt, fixture.id());
        } else {
            jdbc.update("""
                    update content_items
                    set status = 'PUBLISHED', current_revision_id = ?, published_revision_id = ?,
                        first_published_at = ?, last_published_at = ?, updated_at = ?
                    where id = ?
                    """, fixture.revisionId(), fixture.revisionId(), publishedAt, publishedAt,
                    publishedAt, fixture.id());
        }
    }

    private static Fixture fixture(
            String id, String type, String slug, String title, String publishedAt, boolean verified) {
        UUID itemId = UUID.fromString(id);
        UUID revisionId = new UUID(itemId.getMostSignificantBits() + 1, itemId.getLeastSignificantBits());
        return new Fixture(itemId, revisionId, type, slug, title, Instant.parse(publishedAt), verified);
    }

    private record Fixture(
            UUID id, UUID revisionId, String type, String slug, String title,
            Instant publishedAt, boolean verified) {
    }
}
