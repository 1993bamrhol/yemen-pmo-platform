package ye.gov.pmo.organization.dto;

import java.util.UUID;

public record GovernmentEntityResponse(
        UUID id,
        EntityTypeResponse type,
        String officialName,
        String shortName,
        String slug,
        String canonicalPath,
        String status,
        String description,
        String websiteUrl,
        ParentReference parent) {

    public record ParentReference(UUID id, String officialName, String canonicalPath) {
    }
}
