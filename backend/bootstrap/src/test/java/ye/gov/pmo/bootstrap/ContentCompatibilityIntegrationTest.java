package ye.gov.pmo.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.BackfillApplyRequest;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillApplyService;
import ye.gov.pmo.bootstrap.compatibility.ContentCompatibilityRouter;
import ye.gov.pmo.decisions.service.DecisionQuery;
import ye.gov.pmo.decisions.service.DecisionService;
import ye.gov.pmo.documents.service.DocumentQuery;
import ye.gov.pmo.documents.service.DocumentService;
import ye.gov.pmo.news.service.AnnouncementQuery;
import ye.gov.pmo.news.service.AnnouncementService;
import ye.gov.pmo.news.service.NewsQuery;
import ye.gov.pmo.news.service.NewsService;

@SpringBootTest(properties = {
        "features.unified-content-compatibility.news-enabled=true",
        "features.unified-content-compatibility.announcements-enabled=true",
        "features.unified-content-compatibility.decisions-enabled=true",
        "features.unified-content-compatibility.documents-enabled=true"
})
@Transactional
@WithMockUser(username = "admin")
class ContentCompatibilityIntegrationTest {
    @Autowired
    private ContentBackfillApplyService applyService;
    @Autowired
    private ContentCompatibilityRouter router;
    @Autowired
    private NewsQuery news;
    @Autowired
    private NewsService legacyNews;
    @Autowired
    private AnnouncementQuery announcements;
    @Autowired
    private AnnouncementService legacyAnnouncements;
    @Autowired
    private DecisionQuery decisions;
    @Autowired
    private DecisionService legacyDecisions;
    @Autowired
    private DocumentQuery documents;
    @Autowired
    private DocumentService legacyDocuments;

    @Test
    void compatibleUnifiedProjectionPreservesEveryLegacyContract() {
        var expectedNews = legacyNews.findAll();
        var expectedAnnouncements = legacyAnnouncements.findAll();
        var expectedDecisions = legacyDecisions.findAll();
        var expectedDocuments = legacyDocuments.findAll();

        applyService.apply(new BackfillApplyRequest(1, ContentBackfillApplyService.CONFIRMATION));

        assertThat(news.findAll()).isEqualTo(expectedNews);
        assertThat(news.findById(1L)).isEqualTo(expectedNews.getFirst());
        assertThat(announcements.findAll()).isEqualTo(expectedAnnouncements);
        assertThat(announcements.findById(1L)).isEqualTo(expectedAnnouncements.getFirst());
        assertThat(decisions.findAll()).isEqualTo(expectedDecisions);
        assertThat(decisions.findById(1L)).isEqualTo(expectedDecisions.getFirst());
        assertThat(documents.findAll()).isEqualTo(expectedDocuments);
        assertThat(documents.findById(1L)).isEqualTo(expectedDocuments.getFirst());

        assertThat(router.status().comparisonError()).isNull();
        assertThat(router.status().contentTypes()).allSatisfy(type -> {
            assertThat(type.configuredForUnified()).isTrue();
            assertThat(type.shadowReady()).isTrue();
            assertThat(type.effectiveSource()).isEqualTo("UNIFIED");
        });
    }
}
