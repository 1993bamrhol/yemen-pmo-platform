package ye.gov.pmo.bootstrap.compatibility;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

@Component
public class ContentCompatibilityObservability {
    public static final String REQUEST_METRIC = "government.content.compatibility.requests";
    public static final String LATENCY_METRIC = "government.content.compatibility.latency";

    private final MeterRegistry meterRegistry;
    private final Map<ObservationKey, LongAdder> observations = new ConcurrentHashMap<>();

    public ContentCompatibilityObservability(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample start() {
        return Timer.start(meterRegistry);
    }

    public void record(Timer.Sample sample, String contentType, String operation,
                       String source, String fallbackReason) {
        String type = normalize(contentType);
        String normalizedOperation = operation.toLowerCase(Locale.ROOT);
        String normalizedSource = source.toUpperCase(Locale.ROOT);
        String reason = fallbackReason.toUpperCase(Locale.ROOT);
        meterRegistry.counter(REQUEST_METRIC,
                "content_type", type,
                "operation", normalizedOperation,
                "source", normalizedSource,
                "fallback_reason", reason).increment();
        sample.stop(Timer.builder(LATENCY_METRIC)
                .tags("content_type", type, "operation", normalizedOperation, "source", normalizedSource)
                .register(meterRegistry));
        observations.computeIfAbsent(
                new ObservationKey(type, normalizedSource, reason), ignored -> new LongAdder()).increment();
    }

    public TypeObservation snapshot(String contentType) {
        String type = normalize(contentType);
        long legacy = total(type, "LEGACY", null);
        long unified = total(type, "UNIFIED", null);
        long automaticFallbacks = observations.entrySet().stream()
                .filter(entry -> entry.getKey().contentType().equals(type))
                .filter(entry -> entry.getKey().source().equals("LEGACY"))
                .filter(entry -> !entry.getKey().fallbackReason().equals("FLAG_DISABLED"))
                .mapToLong(entry -> entry.getValue().sum())
                .sum();
        Map<String, Long> fallbackReasons = new TreeMap<>();
        observations.forEach((key, count) -> {
            if (key.contentType().equals(type) && key.source().equals("LEGACY")
                    && !key.fallbackReason().equals("FLAG_DISABLED")) {
                fallbackReasons.merge(key.fallbackReason(), count.sum(), Long::sum);
            }
        });
        return new TypeObservation(legacy, unified, automaticFallbacks, Map.copyOf(fallbackReasons));
    }

    private long total(String contentType, String source, String fallbackReason) {
        return observations.entrySet().stream()
                .filter(entry -> entry.getKey().contentType().equals(contentType))
                .filter(entry -> entry.getKey().source().equals(source))
                .filter(entry -> fallbackReason == null
                        || entry.getKey().fallbackReason().equals(fallbackReason))
                .mapToLong(entry -> entry.getValue().sum())
                .sum();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private record ObservationKey(String contentType, String source, String fallbackReason) {
    }

    public record TypeObservation(
            long legacyRequests,
            long unifiedRequests,
            long automaticFallbacks,
            Map<String, Long> fallbackReasons) {
    }
}
