package ye.gov.pmo.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import ye.gov.pmo.content.domain.ContentAction;
import ye.gov.pmo.content.domain.ContentStatus;

@Entity
@Table(name = "content_transitions")
public class ContentTransition {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_item_id", nullable = false)
    private ContentItem contentItem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id")
    private ContentRevision revision;
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 30)
    private ContentStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private ContentStatus toStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ContentAction action;
    @Column(name = "actor_user_id") private Long actorUserId;
    @Column(name = "government_entity_id", nullable = false) private UUID governmentEntityId;
    @Column(name = "comment_text", length = 2000) private String comment;
    @Column(name = "correlation_id", length = 100) private String correlationId;
    @Column(name = "occurred_at", nullable = false) private OffsetDateTime occurredAt;

    protected ContentTransition() {}

    public ContentTransition(ContentItem item, ContentRevision revision, ContentStatus fromStatus,
                             ContentStatus toStatus, ContentAction action, Long actorUserId,
                             UUID governmentEntityId, String comment, String correlationId,
                             OffsetDateTime occurredAt) {
        this.id = UUID.randomUUID();
        this.contentItem = item;
        this.revision = revision;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.action = action;
        this.actorUserId = actorUserId;
        this.governmentEntityId = governmentEntityId;
        this.comment = comment;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public ContentStatus getFromStatus() { return fromStatus; }
    public ContentStatus getToStatus() { return toStatus; }
    public ContentAction getAction() { return action; }
    public Long getActorUserId() { return actorUserId; }
    public String getComment() { return comment; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
}
