package ye.gov.pmo.bootstrap.compatibility;

import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContentCompatibilityExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentCompatibilityExecutor.class);

    private final ContentCompatibilityRouter router;
    private final ContentCompatibilityObservability observability;

    public ContentCompatibilityExecutor(ContentCompatibilityRouter router,
                                        ContentCompatibilityObservability observability) {
        this.router = router;
        this.observability = observability;
    }

    public <T> T execute(String contentType, String operation,
                         Supplier<T> legacyQuery, Supplier<T> unifiedQuery) {
        Timer.Sample sample = observability.start();
        ContentCompatibilityRouter.RouteDecision decision = router.decide(contentType);
        if (!decision.useUnified()) {
            try {
                return legacyQuery.get();
            } finally {
                observability.record(sample, contentType, operation, "LEGACY", decision.reason());
            }
        }
        try {
            T result = unifiedQuery.get();
            observability.record(sample, contentType, operation, "UNIFIED", "NONE");
            return result;
        } catch (RuntimeException exception) {
            LOGGER.warn("Unified content compatibility projection failed; falling back to legacy. type={}, operation={}, error={}",
                    contentType, operation, exception.getClass().getSimpleName());
            try {
                return legacyQuery.get();
            } finally {
                observability.record(sample, contentType, operation, "LEGACY", "PROJECTION_ERROR");
            }
        }
    }
}
