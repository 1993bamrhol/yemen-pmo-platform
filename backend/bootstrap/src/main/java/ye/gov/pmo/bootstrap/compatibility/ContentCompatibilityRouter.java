package ye.gov.pmo.bootstrap.compatibility;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonService;

@Component
public class ContentCompatibilityRouter {
    private static final List<String> TYPES = List.of("NEWS", "ANNOUNCEMENT", "DECISION", "DOCUMENT");

    private final ContentShadowComparisonService shadowComparison;
    private final ContentCompatibilityObservability observability;
    private final Map<String, Boolean> configured;
    private final Duration readinessCacheTtl;
    private volatile ReadinessSnapshot cachedReadiness;

    public ContentCompatibilityRouter(
            ContentShadowComparisonService shadowComparison,
            ContentCompatibilityObservability observability,
            @Value("${features.unified-content-compatibility.news-enabled:false}") boolean news,
            @Value("${features.unified-content-compatibility.announcements-enabled:false}") boolean announcements,
            @Value("${features.unified-content-compatibility.decisions-enabled:false}") boolean decisions,
            @Value("${features.unified-content-compatibility.documents-enabled:false}") boolean documents,
            @Value("${features.unified-content-compatibility.readiness-cache-ttl:30s}") Duration readinessCacheTtl) {
        this.shadowComparison = shadowComparison;
        this.observability = observability;
        this.configured = Map.of(
                "NEWS", news,
                "ANNOUNCEMENT", announcements,
                "DECISION", decisions,
                "DOCUMENT", documents);
        this.readinessCacheTtl = readinessCacheTtl;
    }

    public boolean useUnified(String contentType) {
        return decide(contentType).useUnified();
    }

    public RouteDecision decide(String contentType) {
        String type = normalize(contentType);
        if (!configured.getOrDefault(type, false)) {
            return new RouteDecision(false, "FLAG_DISABLED");
        }
        ReadinessSnapshot snapshot = routingReadiness();
        if (snapshot.comparisonError() != null) {
            return new RouteDecision(false, "SHADOW_ERROR");
        }
        return snapshot.readyByType().getOrDefault(type, false)
                ? new RouteDecision(true, "NONE")
                : new RouteDecision(false, "SHADOW_NOT_READY");
    }

    public StatusReport status() {
        ReadinessSnapshot snapshot = refreshReadiness();
        Map<String, Boolean> ready = snapshot.readyByType();
        String comparisonError = snapshot.comparisonError();
        Map<String, Boolean> finalReady = ready;
        return new StatusReport(Instant.now(), true, comparisonError,
                TYPES.stream().map(type -> {
                    boolean enabled = configured.getOrDefault(type, false);
                    boolean shadowReady = finalReady.getOrDefault(type, false);
                    var observation = observability.snapshot(type);
                    return new TypeStatus(type, enabled, shadowReady,
                            enabled && shadowReady ? "UNIFIED" : "LEGACY",
                            observation.legacyRequests(), observation.unifiedRequests(),
                            observation.automaticFallbacks(), observation.fallbackReasons());
                }).toList());
    }

    private Map<String, Boolean> readiness(ContentShadowComparisonReport report) {
        return report.contentTypes().stream().collect(Collectors.toMap(
                comparison -> normalize(comparison.contentType()),
                ContentShadowComparisonReport.TypeComparison::readyForCanary,
                (left, right) -> left));
    }

    private ReadinessSnapshot routingReadiness() {
        ReadinessSnapshot current = cachedReadiness;
        Instant now = Instant.now();
        if (current != null && current.expiresAt().isAfter(now)) {
            return current;
        }
        synchronized (this) {
            current = cachedReadiness;
            if (current == null || !current.expiresAt().isAfter(now)) {
                current = refreshReadiness();
            }
            return current;
        }
    }

    private ReadinessSnapshot refreshReadiness() {
        Instant now = Instant.now();
        try {
            ReadinessSnapshot snapshot = new ReadinessSnapshot(
                    readiness(shadowComparison.compare()), null, now.plus(readinessCacheTtl));
            cachedReadiness = snapshot;
            return snapshot;
        } catch (RuntimeException exception) {
            ReadinessSnapshot snapshot = new ReadinessSnapshot(
                    Map.of(), exception.getClass().getSimpleName(), now.plus(readinessCacheTtl));
            cachedReadiness = snapshot;
            return snapshot;
        }
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public record StatusReport(
            Instant generatedAt,
            boolean readOnly,
            String comparisonError,
            List<TypeStatus> contentTypes) {
    }

    public record TypeStatus(
            String contentType,
            boolean configuredForUnified,
            boolean shadowReady,
            String effectiveSource,
            long legacyRequests,
            long unifiedRequests,
            long automaticFallbacks,
            Map<String, Long> fallbackReasons) {
    }

    public record RouteDecision(boolean useUnified, String reason) {
    }

    private record ReadinessSnapshot(
            Map<String, Boolean> readyByType,
            String comparisonError,
            Instant expiresAt) {
    }
}
