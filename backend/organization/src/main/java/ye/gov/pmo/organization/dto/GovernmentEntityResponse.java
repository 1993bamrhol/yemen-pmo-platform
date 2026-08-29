package ye.gov.pmo.organization.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GovernmentEntityResponse(
        UUID id,
        String locale,
        EntityTypeResponse type,
        String officialName,
        String officialNameEn,
        String shortName,
        String slug,
        String canonicalPath,
        String status,
        String description,
        String mandate,
        String websiteUrl,
        Contact contact,
        String officialSourceReference,
        OffsetDateTime updatedAt,
        ParentReference parent) {

    public record Contact(String email, String phone, String address) {
    }

    public record ParentReference(UUID id, String officialName, String canonicalPath) {
    }
}
