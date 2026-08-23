package ye.gov.pmo.content.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ye.gov.pmo.content.domain.ContentAction;
import ye.gov.pmo.content.domain.ContentStatus;

public record ContentTransitionResponse(
        UUID id, ContentStatus fromStatus, ContentStatus toStatus,
        ContentAction action, Long actorUserId, String comment, OffsetDateTime occurredAt) {}
