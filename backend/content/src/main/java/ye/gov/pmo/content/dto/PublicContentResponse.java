package ye.gov.pmo.content.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PublicContentResponse(
        UUID id,
        String contentType,
        String slug,
        String locale,
        String canonicalPath,
        String title,
        String summary,
        String body,
        String byline,
        OffsetDateTime publishedAt,
        EntityReference primaryEntity,
        List<CategoryReference> categories) {

    public record EntityReference(UUID id, String officialName, String canonicalPath) {
    }

    public record CategoryReference(UUID id, String slug, String label) {
    }
}
