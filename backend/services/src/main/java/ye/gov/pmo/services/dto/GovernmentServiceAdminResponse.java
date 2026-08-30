package ye.gov.pmo.services.dto;

import java.time.OffsetDateTime;
import ye.gov.pmo.services.domain.ServiceLifecycleStatus;
import ye.gov.pmo.services.domain.ServiceSourceType;
import ye.gov.pmo.services.domain.ServiceVerificationStatus;

public record GovernmentServiceAdminResponse(
        ServiceDetailResponse service,
        ServiceLifecycleStatus lifecycleStatus,
        Verification verification,
        OffsetDateTime firstPublishedAt,
        OffsetDateTime archivedAt,
        OffsetDateTime createdAt,
        long version) {

    public record Verification(
            ServiceVerificationStatus status,
            ServiceSourceType sourceType,
            String sourceReference,
            OffsetDateTime verifiedAt,
            Long verifiedBy) {
    }
}
