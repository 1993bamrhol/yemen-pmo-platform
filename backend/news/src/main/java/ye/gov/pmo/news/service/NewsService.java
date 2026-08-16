package ye.gov.pmo.news.service;

import java.util.List;
import ye.gov.pmo.news.dto.NewsArticleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NewsService {

    private final List<NewsArticleResponse> articles = List.of(
            new NewsArticleResponse(
                    1L,
                    "اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية",
                    "الأخبار",
                    "16 أغسطس 2026",
                    "رئاسة الوزراء تتابع خطوات تنفيذ المنصة الرقمية الموحدة."),
            new NewsArticleResponse(
                    2L,
                    "اعتماد الإطار المؤسسي للبوابة الرسمية",
                    "البيانات",
                    "15 أغسطس 2026",
                    "الوثيقة تحدد الأهداف والفئات المستهدفة والهيكل التنظيمي."),
            new NewsArticleResponse(
                    3L,
                    "إطلاق المرحلة الأولى من المحتوى الرسمي",
                    "القرارات",
                    "14 أغسطس 2026",
                    "بدء نشر الأخبار والقرارات والتعاميم عبر البوابة الجديدة."));

    public List<NewsArticleResponse> findAll() {
        return articles;
    }

    public NewsArticleResponse findById(Long id) {
        return articles.stream()
                .filter(article -> article.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "News article not found with id: " + id));
    }
}
