package ye.gov.pmo.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_assignments")
public class RoleAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    @Column(name = "government_entity_id")
    private UUID governmentEntityId;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RoleAssignment() {
    }

    public RoleAssignment(User user, Role role, ScopeType scopeType, UUID governmentEntityId,
                          OffsetDateTime validFrom, OffsetDateTime validUntil, Long grantedBy) {
        this.user = user;
        this.role = role;
        this.scopeType = scopeType;
        this.governmentEntityId = governmentEntityId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.grantedBy = grantedBy;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public boolean isActiveAt(OffsetDateTime now) {
        return enabled
                && (validFrom == null || !validFrom.isAfter(now))
                && (validUntil == null || validUntil.isAfter(now));
    }

    public void disable() {
        enabled = false;
    }

    public void reactivate(OffsetDateTime validFrom, OffsetDateTime validUntil, Long grantedBy) {
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.grantedBy = grantedBy;
        this.enabled = true;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public Role getRole() { return role; }
    public ScopeType getScopeType() { return scopeType; }
    public UUID getGovernmentEntityId() { return governmentEntityId; }
    public OffsetDateTime getValidFrom() { return validFrom; }
    public OffsetDateTime getValidUntil() { return validUntil; }
    public boolean isEnabled() { return enabled; }
    public Long getGrantedBy() { return grantedBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
