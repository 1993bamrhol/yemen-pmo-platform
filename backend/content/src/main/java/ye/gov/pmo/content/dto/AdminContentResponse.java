package ye.gov.pmo.content.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ye.gov.pmo.content.domain.ContentStatus;
import ye.gov.pmo.content.domain.ContentType;

public record AdminContentResponse(
        UUID id, ContentType contentType, UUID primaryEntityId, String slug, String locale,
        ContentStatus status, Revision currentRevision, Revision publishedRevision,
        OffsetDateTime firstPublishedAt, OffsetDateTime lastPublishedAt, OffsetDateTime archivedAt,
        OffsetDateTime createdAt, OffsetDateTime updatedAt, long version) {
    public record Revision(
            UUID id, int number, String title, String summary, String body,
            String byline, String changeNote, OffsetDateTime createdAt, Long createdBy) {}
}
