package ye.gov.pmo.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_revisions")
public class ContentRevision {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_item_id", nullable = false)
    private ContentItem contentItem;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 255)
    private String byline;

    @Column(name = "change_note", length = 1000)
    private String changeNote;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    protected ContentRevision() {
    }

    public ContentRevision(ContentItem contentItem, int revisionNumber, String title,
                           String summary, String body, String byline, String changeNote,
                           Long createdBy) {
        this.id = UUID.randomUUID();
        this.contentItem = contentItem;
        this.revisionNumber = revisionNumber;
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.byline = byline;
        this.changeNote = changeNote;
        this.createdAt = OffsetDateTime.now();
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public ContentItem getContentItem() { return contentItem; }
    public int getRevisionNumber() { return revisionNumber; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public String getByline() { return byline; }
    public String getChangeNote() { return changeNote; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getCreatedBy() { return createdBy; }
}
