package ye.gov.pmo.organization.dto;

public record EntityTypeResponse(
        Long id,
        String code,
        String name,
        String pathSegment) {
}
