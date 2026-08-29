package ye.gov.pmo.organization.entity;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "government_entities")
public class GovernmentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entity_type_id", nullable = false)
    private EntityType entityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_entity_id")
    private GovernmentEntity parent;

    @Column(name = "official_name_ar", nullable = false, length = 255)
    private String officialNameAr;

    @Column(name = "short_name_ar", length = 150)
    private String shortNameAr;

    @Column(name = "official_name_en", length = 255)
    private String officialNameEn;

    @Column(nullable = false, length = 160)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String mandate;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "official_email", length = 320)
    private String officialEmail;

    @Column(name = "official_phone", length = 80)
    private String officialPhone;

    @Column(name = "official_address_ar", length = 1000)
    private String officialAddressAr;

    @Column(name = "official_source_reference", length = 1000)
    private String officialSourceReference;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected GovernmentEntity() {
    }

    public GovernmentEntity(EntityType entityType, GovernmentEntity parent, String officialNameAr,
                            String officialNameEn, String shortNameAr, String slug, EntityStatus status,
                            String description, String mandate, String websiteUrl, String officialEmail,
                            String officialPhone, String officialAddressAr, String officialSourceReference,
                            Long actorUserId) {
        this.entityType = entityType;
        this.parent = parent;
        this.officialNameAr = officialNameAr;
        this.officialNameEn = officialNameEn;
        this.shortNameAr = shortNameAr;
        this.slug = slug;
        this.status = status;
        this.description = description;
        this.mandate = mandate;
        this.websiteUrl = websiteUrl;
        this.officialEmail = officialEmail;
        this.officialPhone = officialPhone;
        this.officialAddressAr = officialAddressAr;
        this.officialSourceReference = officialSourceReference;
        this.createdBy = actorUserId;
        this.updatedBy = actorUserId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void update(EntityType type, GovernmentEntity parent, String officialNameAr,
                       String officialNameEn, String shortNameAr, String slug, EntityStatus status,
                       String description, String mandate, String websiteUrl, String officialEmail,
                       String officialPhone, String officialAddressAr, String officialSourceReference,
                       Long actorUserId) {
        this.entityType = type;
        this.parent = parent;
        this.officialNameAr = officialNameAr;
        this.officialNameEn = officialNameEn;
        this.shortNameAr = shortNameAr;
        this.slug = slug;
        this.status = status;
        this.description = description;
        this.mandate = mandate;
        this.websiteUrl = websiteUrl;
        this.officialEmail = officialEmail;
        this.officialPhone = officialPhone;
        this.officialAddressAr = officialAddressAr;
        this.officialSourceReference = officialSourceReference;
        this.updatedBy = actorUserId;
    }

    public UUID getId() { return id; }
    public EntityType getEntityType() { return entityType; }
    public GovernmentEntity getParent() { return parent; }
    public String getOfficialNameAr() { return officialNameAr; }
    public String getOfficialNameEn() { return officialNameEn; }
    public String getShortNameAr() { return shortNameAr; }
    public String getSlug() { return slug; }
    public EntityStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getMandate() { return mandate; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getOfficialEmail() { return officialEmail; }
    public String getOfficialPhone() { return officialPhone; }
    public String getOfficialAddressAr() { return officialAddressAr; }
    public String getOfficialSourceReference() { return officialSourceReference; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
