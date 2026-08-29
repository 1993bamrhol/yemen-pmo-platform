package ye.gov.pmo.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "government_entity_slug_aliases")
public class GovernmentEntitySlugAlias {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "government_entity_id", nullable = false)
    private GovernmentEntity governmentEntity;

    @Column(name = "public_path_segment", nullable = false, length = 80)
    private String publicPathSegment;

    @Column(nullable = false, length = 160)
    private String slug;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    protected GovernmentEntitySlugAlias() {
    }

    public GovernmentEntitySlugAlias(GovernmentEntity governmentEntity, String publicPathSegment,
                                     String slug, Long createdBy) {
        this.governmentEntity = governmentEntity;
        this.publicPathSegment = publicPathSegment;
        this.slug = slug;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public GovernmentEntity getGovernmentEntity() { return governmentEntity; }
    public String getPublicPathSegment() { return publicPathSegment; }
    public String getSlug() { return slug; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
