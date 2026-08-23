package ye.gov.pmo.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import ye.gov.pmo.content.domain.ContentStatus;
import ye.gov.pmo.content.domain.ContentType;
import ye.gov.pmo.organization.entity.GovernmentEntity;

@Entity(name = "UnifiedContentItem")
@Table(name = "content_items")
public class ContentItem {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 40)
    private ContentType contentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_entity_id", nullable = false)
    private GovernmentEntity primaryEntity;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(nullable = false, length = 12)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_revision_id")
    private ContentRevision currentRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_revision_id")
    private ContentRevision publishedRevision;

    @Column(name = "display_metadata", columnDefinition = "TEXT")
    private String displayMetadata;

    @Column(name = "first_published_at")
    private OffsetDateTime firstPublishedAt;

    @Column(name = "last_published_at")
    private OffsetDateTime lastPublishedAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Version
    @Column(nullable = false)
    private long version;

    protected ContentItem() {
    }

    public ContentItem(ContentType contentType, GovernmentEntity primaryEntity, String slug,
                       String locale, Long actorUserId) {
        this.id = UUID.randomUUID();
        this.contentType = contentType;
        this.primaryEntity = primaryEntity;
        this.slug = slug;
        this.locale = locale;
        this.status = ContentStatus.DRAFT;
        this.createdBy = actorUserId;
        this.updatedBy = actorUserId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void setCurrentRevision(ContentRevision revision, Long actorUserId) {
        this.currentRevision = revision;
        this.updatedBy = actorUserId;
    }

    public void transitionTo(ContentStatus target, Long actorUserId, OffsetDateTime occurredAt) {
        this.status = target;
        this.updatedBy = actorUserId;
        if (target == ContentStatus.PUBLISHED) {
            this.publishedRevision = currentRevision;
            if (firstPublishedAt == null) firstPublishedAt = occurredAt;
            lastPublishedAt = occurredAt;
            archivedAt = null;
        } else if (target == ContentStatus.ARCHIVED) {
            archivedAt = occurredAt;
        }
    }

    public UUID getId() { return id; }
    public ContentType getContentType() { return contentType; }
    public GovernmentEntity getPrimaryEntity() { return primaryEntity; }
    public String getSlug() { return slug; }
    public String getLocale() { return locale; }
    public ContentStatus getStatus() { return status; }
    public ContentRevision getCurrentRevision() { return currentRevision; }
    public ContentRevision getPublishedRevision() { return publishedRevision; }
    public OffsetDateTime getFirstPublishedAt() { return firstPublishedAt; }
    public OffsetDateTime getLastPublishedAt() { return lastPublishedAt; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedBy() { return createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public long getVersion() { return version; }
}
