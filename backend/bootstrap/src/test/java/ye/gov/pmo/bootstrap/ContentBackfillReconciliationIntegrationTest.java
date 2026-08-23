package ye.gov.pmo.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.BackfillApplyRequest;
import ye.gov.pmo.bootstrap.backfill.BackfillApplyResponse;
import ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport;
import ye.gov.pmo.bootstrap.backfill.BackfillManifest;
import ye.gov.pmo.bootstrap.backfill.BackfillManifestLoader;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillReconciliationService;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillApplyService;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonService;

@SpringBootTest
@AutoConfigureMockMvc
class ContentBackfillReconciliationIntegrationTest {

    @Autowired
    private ContentBackfillReconciliationService reconciliation;

    @Autowired
    private BackfillManifestLoader manifestLoader;

    @Autowired
    private ContentBackfillApplyService applyService;

    @Autowired
    private ContentShadowComparisonService shadowComparison;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void approvedDryRunCoversEverySourceWithoutWriting() {
        int contentBefore = count("content_items");
        int mappingsBefore = count("legacy_content_mappings");

        BackfillReconciliationReport report = reconciliation.reconcile();

        assertThat(report.dryRun()).isTrue();
        assertThat(report.readyToApply()).isTrue();
        assertThat(report.counts().discoveredSources()).isEqualTo(17);
        assertThat(report.counts().manifestEntries()).isEqualTo(17);
        assertThat(report.counts().createActions()).isEqualTo(12);
        assertThat(report.counts().mergeActions()).isEqualTo(2);
        assertThat(report.counts().skipActions()).isEqualTo(3);
        assertThat(report.counts().projectedContentItems()).isEqualTo(12);
        assertThat(report.counts().blockers()).isZero();
        assertThat(report.counts().warnings()).isZero();
        assertThat(report.databaseState().canonicalSlugCollisions()).isZero();
        assertThat(report.databaseState().orphanMappings()).isZero();

        Set<String> issueCodes = report.issues().stream()
                .map(BackfillReconciliationReport.Issue::code)
                .collect(Collectors.toSet());
        assertThat(issueCodes).doesNotContain(
                "UNMAPPED_LIVE_SOURCE", "MISSING_LIVE_SOURCE", "DUPLICATE_MANIFEST_SOURCE",
                "DUPLICATE_MANIFEST_SLUG", "MERGE_TARGET_NOT_FOUND", "MERGE_STATUS_CONFLICT",
                "SOURCE_DRIFT_TITLE", "SOURCE_DRIFT_STATUS", "SOURCE_DRIFT_BYLINE");

        assertThat(issueCodes).doesNotContain("MISSING_BODY_SOURCE");

        assertThat(count("content_items")).isEqualTo(contentBefore);
        assertThat(count("legacy_content_mappings")).isEqualTo(mappingsBefore);
    }

    @Test
    void manifestMakesDuplicateAndMergeDecisionsExplicit() {
        BackfillManifest manifest = manifestLoader.load();

        assertThat(manifest.entries()).hasSize(17);
        assertThat(manifest.entries()).extracting(BackfillManifest.Entry::sourceKey)
                .doesNotHaveDuplicates();
        assertThat(manifest.entries().stream()
                .filter(entry -> entry.action() == BackfillManifest.Action.CREATE)
                .map(BackfillManifest.Entry::canonicalKey))
                .hasSize(12)
                .doesNotHaveDuplicates();
        assertThat(manifest.entries().stream()
                .filter(entry -> entry.action() == BackfillManifest.Action.MERGE_INTO)
                .map(BackfillManifest.Entry::sourceKey))
                .containsExactlyInAnyOrder("STATIC_NEWS:NEWS:1", "STATIC_DECISIONS:DECISION:1");

        BackfillManifest.Entry adminDraft = manifest.entries().stream()
                .filter(entry -> entry.sourceKey().equals("ADMIN_CONTENT:ANNOUNCEMENT:2"))
                .findFirst().orElseThrow();
        BackfillManifest.Entry publicAnnouncement = manifest.entries().stream()
                .filter(entry -> entry.sourceKey().equals("STATIC_ANNOUNCEMENTS:ANNOUNCEMENT:1"))
                .findFirst().orElseThrow();
        assertThat(adminDraft.expectedStatus()).isEqualTo("DRAFT");
        assertThat(adminDraft.action()).isEqualTo(BackfillManifest.Action.SKIP_WITH_REASON);
        assertThat(adminDraft.skipReason()).isNotBlank();
        assertThat(publicAnnouncement.expectedStatus()).isEqualTo("PUBLISHED");
        assertThat(adminDraft.canonicalKey()).isNotEqualTo(publicAnnouncement.canonicalKey());
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin")
    void applyIsTransactionalIdempotentAndPreservesLegacySources() {
        int legacyAdminBefore = count("admin_content");

        BackfillApplyRequest request = new BackfillApplyRequest(
                1, ContentBackfillApplyService.CONFIRMATION);
        BackfillApplyResponse first = applyService.apply(request);

        assertThat(first.executed()).isTrue();
        assertThat(first.createdContentItems()).isEqualTo(12);
        assertThat(first.createdMappings()).isEqualTo(14);
        assertThat(first.skippedSources()).isEqualTo(3);
        assertThat(count("content_items")).isEqualTo(12);
        assertThat(count("content_revisions")).isEqualTo(12);
        assertThat(count("legacy_content_mappings")).isEqualTo(14);
        assertThat(count("content_taxonomy_assignments")).isEqualTo(13);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from content_items where status = 'PUBLISHED' and published_revision_id is not null",
                Integer.class)).isEqualTo(12);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from content_revisions where body is null or trim(body) = ''",
                Integer.class)).isZero();
        assertThat(count("admin_content")).isEqualTo(legacyAdminBefore);

        BackfillApplyResponse second = applyService.apply(request);

        assertThat(second.executed()).isFalse();
        assertThat(second.createdContentItems()).isZero();
        assertThat(second.existingContentItems()).isEqualTo(12);
        assertThat(second.createdMappings()).isZero();
        assertThat(second.existingMappings()).isEqualTo(14);
        assertThat(count("content_items")).isEqualTo(12);
        assertThat(count("legacy_content_mappings")).isEqualTo(14);

        int auditEventsBeforeComparison = count("audit_events");
        ContentShadowComparisonReport shadow = shadowComparison.compare();
        assertThat(shadow.readOnly()).isTrue();
        assertThat(shadow.reconciliationReady()).isTrue();
        assertThat(shadow.readyForCanary()).isTrue();
        assertThat(shadow.legacyPublicSources()).isEqualTo(12);
        assertThat(shadow.mappedUnifiedItems()).isEqualTo(12);
        assertThat(shadow.differences()).isZero();
        assertThat(shadow.contentTypes()).hasSize(4).allSatisfy(type -> {
            assertThat(type.legacyCount()).isEqualTo(3);
            assertThat(type.mappedCount()).isEqualTo(3);
            assertThat(type.unifiedPublishedCount()).isEqualTo(3);
            assertThat(type.additionalUnifiedItems()).isZero();
            assertThat(type.countParity()).isTrue();
            assertThat(type.orderParity()).isTrue();
            assertThat(type.fieldParity()).isTrue();
            assertThat(type.readyForCanary()).isTrue();
            assertThat(type.items()).allSatisfy(item -> assertThat(item.differences()).isEmpty());
        });
        assertThat(shadow.portalHome().comparedSections()).isEqualTo(3);
        assertThat(shadow.portalHome().matchedSections()).isEqualTo(3);
        assertThat(shadow.portalHome().contentProjectionReady()).isTrue();
        assertThat(count("audit_events")).isEqualTo(auditEventsBeforeComparison);
        assertThat(count("content_items")).isEqualTo(12);
    }

    @Test
    void reconciliationEndpointRequiresPlatformContentManagementPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content-backfill/reconciliation"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/content-backfill/reconciliation")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.readyToApply").value(true))
                .andExpect(jsonPath("$.counts.discoveredSources").value(17))
                .andExpect(jsonPath("$.counts.blockers").value(0));
    }

    @Test
    void applyEndpointRejectsMissingAuthenticationAndInvalidConfirmation() throws Exception {
        String invalidRequest = """
                {"manifestSchemaVersion":1,"confirmation":"INVALID"}
                """;
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                        "/api/v1/admin/content-backfill/apply")
                        .contentType("application/json")
                        .content(invalidRequest))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                        "/api/v1/admin/content-backfill/apply")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(invalidRequest))
                .andExpect(status().isConflict());
    }

    @Test
    void shadowComparisonEndpointIsReadOnlyAndRequiresPlatformPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content-shadow-comparison"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/content-shadow-comparison").with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readOnly").value(true));
    }

    @Test
    void compatibilityStatusIsProtectedAndDefaultsEveryTypeToLegacy() throws Exception {
        mockMvc.perform(get("/api/v1/admin/content-compatibility/status"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/content-compatibility/status").with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readOnly").value(true))
                .andExpect(jsonPath("$.contentTypes.length()").value(4))
                .andExpect(jsonPath("$.contentTypes[*].configuredForUnified",
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))))
                .andExpect(jsonPath("$.contentTypes[*].effectiveSource",
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("LEGACY"))));
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }
}
