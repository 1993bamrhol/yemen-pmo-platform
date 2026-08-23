package ye.gov.pmo.bootstrap.compatibility;

import java.time.Instant;
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
    private final Map<String, Boolean> configured;

    public ContentCompatibilityRouter(
            ContentShadowComparisonService shadowComparison,
            @Value("${features.unified-content-compatibility.news-enabled:false}") boolean news,
            @Value("${features.unified-content-compatibility.announcements-enabled:false}") boolean announcements,
            @Value("${features.unified-content-compatibility.decisions-enabled:false}") boolean decisions,
            @Value("${features.unified-content-compatibility.documents-enabled:false}") boolean documents) {
        this.shadowComparison = shadowComparison;
        this.configured = Map.of(
                "NEWS", news,
                "ANNOUNCEMENT", announcements,
                "DECISION", decisions,
                "DOCUMENT", documents);
    }

    public boolean useUnified(String contentType) {
        String type = normalize(contentType);
        if (!configured.getOrDefault(type, false)) {
            return false;
        }
        try {
            return readiness(shadowComparison.compare()).getOrDefault(type, false);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public StatusReport status() {
        Map<String, Boolean> ready;
        String comparisonError = null;
        try {
            ready = readiness(shadowComparison.compare());
        } catch (RuntimeException exception) {
            ready = Map.of();
            comparisonError = exception.getClass().getSimpleName();
        }
        Map<String, Boolean> finalReady = ready;
        return new StatusReport(Instant.now(), true, comparisonError,
                TYPES.stream().map(type -> {
                    boolean enabled = configured.getOrDefault(type, false);
                    boolean shadowReady = finalReady.getOrDefault(type, false);
                    return new TypeStatus(type, enabled, shadowReady,
                            enabled && shadowReady ? "UNIFIED" : "LEGACY");
                }).toList());
    }

    private Map<String, Boolean> readiness(ContentShadowComparisonReport report) {
        return report.contentTypes().stream().collect(Collectors.toMap(
                comparison -> normalize(comparison.contentType()),
                ContentShadowComparisonReport.TypeComparison::readyForCanary,
                (left, right) -> left));
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
            String effectiveSource) {
    }
}
