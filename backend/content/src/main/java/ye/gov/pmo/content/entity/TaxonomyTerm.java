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
@Table(name = "taxonomy_terms")
public class TaxonomyTerm {

    @Id
    private UUID id;

    @Column(name = "taxonomy_code", nullable = false, length = 50)
    private String taxonomyCode;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "label_ar", nullable = false, length = 180)
    private String labelAr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_term_id")
    private TaxonomyTerm parent;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TaxonomyTerm() {
    }

    public UUID getId() { return id; }
    public String getTaxonomyCode() { return taxonomyCode; }
    public String getSlug() { return slug; }
    public String getLabelAr() { return labelAr; }
    public boolean isActive() { return active; }
}
