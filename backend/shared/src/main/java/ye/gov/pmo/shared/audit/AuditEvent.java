package ye.gov.pmo.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;

    @Column(name = "resource_id", length = 120)
    private String resourceId;

    @Column(name = "government_entity_id")
    private UUID governmentEntityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditOutcome outcome;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(Long actorUserId, String action, String resourceType, String resourceId,
                      UUID governmentEntityId, AuditOutcome outcome, String correlationId, String metadata) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.governmentEntityId = governmentEntityId;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.metadata = metadata;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public UUID getGovernmentEntityId() {
        return governmentEntityId;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
