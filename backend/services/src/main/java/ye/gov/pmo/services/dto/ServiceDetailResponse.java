package ye.gov.pmo.services.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ServiceDetailResponse(
        UUID id,
        String locale,
        String slug,
        String canonicalPath,
        String officialName,
        String officialNameEn,
        String summary,
        String description,
        ServiceOwnerResponse ownerEntity,
        List<ServiceDetailItemResponse> eligibility,
        List<ServiceDetailItemResponse> requirements,
        List<ServiceDetailItemResponse> steps,
        String fees,
        String processingTime,
        List<ServiceChannelResponse> channels,
        ServiceSourceResponse source,
        OffsetDateTime publishedAt,
        OffsetDateTime updatedAt) {
}
