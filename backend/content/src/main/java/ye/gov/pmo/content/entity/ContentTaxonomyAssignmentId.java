package ye.gov.pmo.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ContentTaxonomyAssignmentId implements Serializable {

    @Column(name = "content_item_id")
    private UUID contentItemId;

    @Column(name = "taxonomy_term_id")
    private UUID taxonomyTermId;

    protected ContentTaxonomyAssignmentId() {
    }

    public ContentTaxonomyAssignmentId(UUID contentItemId, UUID taxonomyTermId) {
        this.contentItemId = contentItemId;
        this.taxonomyTermId = taxonomyTermId;
    }

    public UUID getContentItemId() { return contentItemId; }
    public UUID getTaxonomyTermId() { return taxonomyTermId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ContentTaxonomyAssignmentId that)) return false;
        return Objects.equals(contentItemId, that.contentItemId)
                && Objects.equals(taxonomyTermId, that.taxonomyTermId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentItemId, taxonomyTermId);
    }
}
