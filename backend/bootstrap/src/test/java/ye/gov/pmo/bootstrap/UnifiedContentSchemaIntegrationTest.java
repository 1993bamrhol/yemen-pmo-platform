package ye.gov.pmo.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class UnifiedContentSchemaIntegrationTest {

    private static final UUID PMO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrationCreatesUnifiedContentSchemaWithoutBackfillingYet() {
        Integer mappingCount = jdbcTemplate.queryForObject(
                "select count(*) from legacy_content_mappings", Integer.class);
        Integer legacyCount = jdbcTemplate.queryForObject("select count(*) from admin_content", Integer.class);
        Integer tableCount = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.tables
                where lower(table_name) in (
                    'content_items', 'content_revisions', 'content_entity_links',
                    'taxonomy_terms', 'content_taxonomy_assignments', 'content_attachments',
                    'decision_details', 'document_details', 'content_slug_redirects',
                    'legacy_content_mappings', 'content_transitions'
                )
                """, Integer.class);

        assertEquals(0, mappingCount);
        assertEquals(5, legacyCount);
        assertEquals(11, tableCount);
    }

    @Test
    void databaseEnforcesCanonicalSlugUniqueness() {
        insertDraft(UUID.randomUUID(), "shared-slug");
        assertThrows(DataIntegrityViolationException.class,
                () -> insertDraft(UUID.randomUUID(), "shared-slug"));
    }

    @Test
    void databaseRejectsPublishedItemWithoutPublishedRevision() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, 'NEWS', ?, ?, 'ar', 'PUBLISHED', ?, ?, 0)
                """, id, PMO_ID, "invalid-published-" + id,
                Timestamp.from(now.toInstant()), Timestamp.from(now.toInstant())));
    }

    private void insertDraft(UUID id, String slug) {
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update("""
                insert into content_items (
                    id, content_type, primary_entity_id, slug, locale, status,
                    created_at, updated_at, version
                ) values (?, 'NEWS', ?, ?, 'ar', 'DRAFT', ?, ?, 0)
                """, id, PMO_ID, slug,
                Timestamp.from(now.toInstant()), Timestamp.from(now.toInstant()));
    }
}
