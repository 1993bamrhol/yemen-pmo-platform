package ye.gov.pmo.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "entity_relationships")
public class EntityRelationship {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private GovernmentEntity source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private GovernmentEntity target;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 40)
    private EntityRelationshipType relationshipType;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    protected EntityRelationship() {
    }

    public EntityRelationship(GovernmentEntity source, GovernmentEntity target,
                              EntityRelationshipType relationshipType, LocalDate validFrom,
                              LocalDate validTo, Long createdBy) {
        this.source = source;
        this.target = target;
        this.relationshipType = relationshipType;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public GovernmentEntity getSource() { return source; }
    public GovernmentEntity getTarget() { return target; }
    public EntityRelationshipType getRelationshipType() { return relationshipType; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
