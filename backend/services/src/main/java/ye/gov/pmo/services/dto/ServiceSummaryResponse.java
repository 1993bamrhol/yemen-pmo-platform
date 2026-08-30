package ye.gov.pmo.services.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ye.gov.pmo.services.domain.ServiceDeliveryChannel;

public record ServiceSummaryResponse(
        UUID id,
        String locale,
        String slug,
        String canonicalPath,
        String officialName,
        String officialNameEn,
        String summary,
        ServiceOwnerResponse ownerEntity,
        List<ServiceDeliveryChannel> channels,
        OffsetDateTime updatedAt) {
}
