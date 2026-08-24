package ye.gov.pmo.bootstrap.compatibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonService;

class ContentCompatibilityRouterTest {

    @Test
    void readinessIsCachedAcrossRequestsWithinTtl() {
        ContentShadowComparisonService shadow = mock(ContentShadowComparisonService.class);
        ContentShadowComparisonReport report = mock(ContentShadowComparisonReport.class);
        ContentShadowComparisonReport.TypeComparison news = mock(
                ContentShadowComparisonReport.TypeComparison.class);
        when(news.contentType()).thenReturn("NEWS");
        when(news.readyForCanary()).thenReturn(true);
        when(report.contentTypes()).thenReturn(List.of(news));
        when(shadow.compare()).thenReturn(report);
        ContentCompatibilityRouter router = router(shadow, true);

        assertThat(router.decide("NEWS").useUnified()).isTrue();
        assertThat(router.decide("NEWS").useUnified()).isTrue();

        verify(shadow).compare();
    }

    @Test
    void disabledFlagDoesNotRunShadowComparison() {
        ContentShadowComparisonService shadow = mock(ContentShadowComparisonService.class);
        ContentCompatibilityRouter router = router(shadow, false);

        assertThat(router.decide("NEWS"))
                .isEqualTo(new ContentCompatibilityRouter.RouteDecision(false, "FLAG_DISABLED"));

        verify(shadow, never()).compare();
    }

    @Test
    void comparisonFailureFailsClosedAndIsCached() {
        ContentShadowComparisonService shadow = mock(ContentShadowComparisonService.class);
        when(shadow.compare()).thenThrow(new IllegalStateException("database unavailable"));
        ContentCompatibilityRouter router = router(shadow, true);

        assertThat(router.decide("NEWS").reason()).isEqualTo("SHADOW_ERROR");
        assertThat(router.decide("NEWS").reason()).isEqualTo("SHADOW_ERROR");

        verify(shadow).compare();
    }

    private ContentCompatibilityRouter router(ContentShadowComparisonService shadow, boolean newsEnabled) {
        var observability = new ContentCompatibilityObservability(new SimpleMeterRegistry());
        return new ContentCompatibilityRouter(shadow, observability, newsEnabled,
                false, false, false, Duration.ofSeconds(30));
    }
}
