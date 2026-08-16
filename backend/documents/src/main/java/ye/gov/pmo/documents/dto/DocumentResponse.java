package ye.gov.pmo.documents.dto;

public record DocumentResponse(
        Long id,
        String title,
        String category,
        String updatedAt,
        String description) {
}
