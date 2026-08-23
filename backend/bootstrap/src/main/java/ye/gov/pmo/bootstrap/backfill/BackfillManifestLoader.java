package ye.gov.pmo.bootstrap.backfill;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class BackfillManifestLoader {
    private static final String MANIFEST_PATH = "backfill/unified-content-v1.json";

    private final ObjectMapper objectMapper;

    public BackfillManifestLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BackfillManifest load() {
        try (var input = new ClassPathResource(MANIFEST_PATH).getInputStream()) {
            return objectMapper.readValue(input, BackfillManifest.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read unified content backfill manifest", exception);
        }
    }
}
