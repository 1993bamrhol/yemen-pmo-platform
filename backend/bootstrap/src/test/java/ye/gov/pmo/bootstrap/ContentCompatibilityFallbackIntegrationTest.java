package ye.gov.pmo.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ye.gov.pmo.bootstrap.compatibility.ContentCompatibilityObservability;
import ye.gov.pmo.bootstrap.compatibility.ContentCompatibilityRouter;
import ye.gov.pmo.news.service.NewsQuery;
import ye.gov.pmo.news.service.NewsService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:compatibility_fallback;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "features.unified-content-compatibility.news-enabled=true",
        "features.unified-content-compatibility.announcements-enabled=false",
        "features.unified-content-compatibility.decisions-enabled=false",
        "features.unified-content-compatibility.documents-enabled=false"
})
class ContentCompatibilityFallbackIntegrationTest {
    @Autowired
    private NewsQuery news;
    @Autowired
    private NewsService legacyNews;
    @Autowired
    private ContentCompatibilityRouter router;
    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void enabledFlagFallsBackAndRecordsReasonWhenShadowIsNotReady() {
        assertThat(news.findAll()).isEqualTo(legacyNews.findAll());

        var newsStatus = router.status().contentTypes().stream()
                .filter(type -> type.contentType().equals("NEWS"))
                .findFirst().orElseThrow();
        assertThat(newsStatus.configuredForUnified()).isTrue();
        assertThat(newsStatus.shadowReady()).isFalse();
        assertThat(newsStatus.effectiveSource()).isEqualTo("LEGACY");
        assertThat(newsStatus.legacyRequests()).isEqualTo(1);
        assertThat(newsStatus.unifiedRequests()).isZero();
        assertThat(newsStatus.automaticFallbacks()).isEqualTo(1);
        assertThat(meterRegistry.get(ContentCompatibilityObservability.REQUEST_METRIC)
                .tag("content_type", "NEWS")
                .tag("operation", "list")
                .tag("source", "LEGACY")
                .tag("fallback_reason", "SHADOW_NOT_READY")
                .counter().count()).isEqualTo(1.0);
    }
}
