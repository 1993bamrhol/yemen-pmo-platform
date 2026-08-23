package ye.gov.pmo.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "content_taxonomy_assignments")
public class ContentTaxonomyAssignment {

    @EmbeddedId
    private ContentTaxonomyAssignmentId id;

    @MapsId("contentItemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_item_id", nullable = false)
    private ContentItem contentItem;

    @MapsId("taxonomyTermId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "taxonomy_term_id", nullable = false)
    private TaxonomyTerm term;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    protected ContentTaxonomyAssignment() {
    }

    public ContentTaxonomyAssignment(ContentItem contentItem, TaxonomyTerm term, Long createdBy) {
        this.id = new ContentTaxonomyAssignmentId(contentItem.getId(), term.getId());
        this.contentItem = contentItem;
        this.term = term;
        this.createdAt = OffsetDateTime.now();
        this.createdBy = createdBy;
    }

    public ContentItem getContentItem() { return contentItem; }
    public TaxonomyTerm getTerm() { return term; }
}
