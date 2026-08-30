package ye.gov.pmo.services.entity;

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
import java.util.Objects;
import java.util.UUID;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.services.domain.ServiceLifecycleStatus;
import ye.gov.pmo.services.domain.ServiceSourceType;
import ye.gov.pmo.services.domain.ServiceVerificationStatus;

@Entity
@Table(name = "government_services")
public class GovernmentService {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owning_entity_id", nullable = false)
    private GovernmentEntity owningEntity;

    @Column(nullable = false, length = 160, unique = true)
    private String slug;

    @Column(name = "official_name_ar", nullable = false, length = 255)
    private String officialNameAr;

    @Column(name = "official_name_en", length = 255)
    private String officialNameEn;

    @Column(name = "summary_ar", length = 1000)
    private String summaryAr;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "fees_ar", length = 2000)
    private String feesAr;

    @Column(name = "processing_time_ar", length = 1000)
    private String processingTimeAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    private ServiceLifecycleStatus lifecycleStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private ServiceVerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "provenance_source_type", length = 40)
    private ServiceSourceType provenanceSourceType;

    @Column(name = "provenance_source_reference", length = 1000)
    private String provenanceSourceReference;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;

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

    protected GovernmentService() {
    }

    public GovernmentService(GovernmentEntity owningEntity, String slug, String officialNameAr,
                             String officialNameEn, String summaryAr, String descriptionAr,
                             String feesAr, String processingTimeAr, Long actorUserId) {
        this.id = UUID.randomUUID();
        this.owningEntity = Objects.requireNonNull(owningEntity);
        this.slug = slug;
        this.officialNameAr = officialNameAr;
        this.officialNameEn = officialNameEn;
        this.summaryAr = summaryAr;
        this.descriptionAr = descriptionAr;
        this.feesAr = feesAr;
        this.processingTimeAr = processingTimeAr;
        this.lifecycleStatus = ServiceLifecycleStatus.DRAFT;
        this.verificationStatus = ServiceVerificationStatus.UNVERIFIED;
        this.createdBy = actorUserId;
        this.updatedBy = actorUserId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (lifecycleStatus == null) lifecycleStatus = ServiceLifecycleStatus.DRAFT;
        if (verificationStatus == null) verificationStatus = ServiceVerificationStatus.UNVERIFIED;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void updateProfile(GovernmentEntity owner, String officialNameAr, String officialNameEn,
                              String summaryAr, String descriptionAr, String feesAr,
                              String processingTimeAr, Long actorUserId) {
        this.owningEntity = Objects.requireNonNull(owner);
        this.officialNameAr = officialNameAr;
        this.officialNameEn = officialNameEn;
        this.summaryAr = summaryAr;
        this.descriptionAr = descriptionAr;
        this.feesAr = feesAr;
        this.processingTimeAr = processingTimeAr;
        this.updatedBy = actorUserId;
        clearVerification(ServiceVerificationStatus.UNVERIFIED);
    }

    public void publish(Long actorUserId, OffsetDateTime occurredAt) {
        lifecycleStatus = ServiceLifecycleStatus.PUBLISHED;
        clearVerification(ServiceVerificationStatus.UNVERIFIED);
        if (firstPublishedAt == null) firstPublishedAt = occurredAt;
        lastPublishedAt = occurredAt;
        archivedAt = null;
        updatedBy = actorUserId;
    }

    public void archive(Long actorUserId, OffsetDateTime occurredAt) {
        lifecycleStatus = ServiceLifecycleStatus.ARCHIVED;
        clearVerification(ServiceVerificationStatus.UNVERIFIED);
        archivedAt = occurredAt;
        updatedBy = actorUserId;
    }

    public void updateVerification(ServiceVerificationStatus status, ServiceSourceType sourceType,
                                   String sourceReference, Long actorUserId, OffsetDateTime occurredAt) {
        Objects.requireNonNull(status);
        if (status == ServiceVerificationStatus.UNVERIFIED) {
            clearVerification(status);
            updatedBy = actorUserId;
            return;
        }
        if (lifecycleStatus != ServiceLifecycleStatus.PUBLISHED) {
            throw new IllegalStateException("Only published services can receive a verification decision");
        }
        if (status == ServiceVerificationStatus.REJECTED) {
            clearVerification(status);
            updatedBy = actorUserId;
            return;
        }
        if (sourceType == null || sourceReference == null || sourceReference.isBlank()) {
            throw new IllegalArgumentException("Verified services require provenance");
        }
        verificationStatus = ServiceVerificationStatus.VERIFIED;
        provenanceSourceType = sourceType;
        provenanceSourceReference = sourceReference.trim();
        verifiedAt = occurredAt;
        verifiedBy = actorUserId;
        updatedBy = actorUserId;
    }

    public boolean isPubliclyEligible() {
        return lifecycleStatus == ServiceLifecycleStatus.PUBLISHED
                && verificationStatus == ServiceVerificationStatus.VERIFIED;
    }

    private void clearVerification(ServiceVerificationStatus status) {
        verificationStatus = status;
        provenanceSourceType = null;
        provenanceSourceReference = null;
        verifiedAt = null;
        verifiedBy = null;
    }

    public UUID getId() { return id; }
    public GovernmentEntity getOwningEntity() { return owningEntity; }
    public String getSlug() { return slug; }
    public String getOfficialNameAr() { return officialNameAr; }
    public String getOfficialNameEn() { return officialNameEn; }
    public String getSummaryAr() { return summaryAr; }
    public String getDescriptionAr() { return descriptionAr; }
    public String getFeesAr() { return feesAr; }
    public String getProcessingTimeAr() { return processingTimeAr; }
    public ServiceLifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public ServiceVerificationStatus getVerificationStatus() { return verificationStatus; }
    public ServiceSourceType getProvenanceSourceType() { return provenanceSourceType; }
    public String getProvenanceSourceReference() { return provenanceSourceReference; }
    public OffsetDateTime getVerifiedAt() { return verifiedAt; }
    public Long getVerifiedBy() { return verifiedBy; }
    public OffsetDateTime getFirstPublishedAt() { return firstPublishedAt; }
    public OffsetDateTime getLastPublishedAt() { return lastPublishedAt; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedBy() { return createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public long getVersion() { return version; }
}
