package ye.gov.pmo.bootstrap.compatibility;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ye.gov.pmo.news.dto.NewsArticleResponse;
import ye.gov.pmo.news.service.NewsQuery;
import ye.gov.pmo.news.service.NewsService;

@Service
@Primary
public class NewsCompatibilityQuery implements NewsQuery {
    private final NewsService legacy;
    private final UnifiedLegacyProjectionService unified;
    private final ContentCompatibilityRouter router;

    public NewsCompatibilityQuery(NewsService legacy, UnifiedLegacyProjectionService unified,
                                  ContentCompatibilityRouter router) {
        this.legacy = legacy;
        this.unified = unified;
        this.router = router;
    }

    @Override
    public List<NewsArticleResponse> findAll() {
        if (!router.useUnified("NEWS")) return legacy.findAll();
        try {
            return unified.findAll("NEWS", "STATIC_NEWS").stream().map(item ->
                    new NewsArticleResponse(item.id(), item.title(), item.category(), item.date(), item.summary())).toList();
        } catch (RuntimeException exception) {
            return legacy.findAll();
        }
    }

    @Override
    public NewsArticleResponse findById(Long id) {
        if (!router.useUnified("NEWS")) return legacy.findById(id);
        try {
            var item = unified.findById("NEWS", "STATIC_NEWS", id);
            return new NewsArticleResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        } catch (RuntimeException exception) {
            return legacy.findById(id);
        }
    }
}
