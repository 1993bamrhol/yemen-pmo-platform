package ye.gov.pmo.news.dto;

public record AnnouncementResponse(
        Long id,
        String title,
        String category,
        String date,
        String excerpt) {
}
