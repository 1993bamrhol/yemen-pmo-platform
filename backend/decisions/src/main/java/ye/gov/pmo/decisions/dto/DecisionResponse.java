package ye.gov.pmo.decisions.dto;

public record DecisionResponse(
        Long id,
        String title,
        String category,
        String date,
        String description) {
}
