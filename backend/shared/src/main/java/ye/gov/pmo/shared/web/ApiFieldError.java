package ye.gov.pmo.shared.web;

public record ApiFieldError(
        String field,
        String reason,
        String message) {
}
