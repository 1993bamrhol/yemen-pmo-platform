package ye.gov.pmo.news.dto;

public record NewsArticleResponse(
        Long id,
        String title,
        String category,
        String date,
        String excerpt) {
}
