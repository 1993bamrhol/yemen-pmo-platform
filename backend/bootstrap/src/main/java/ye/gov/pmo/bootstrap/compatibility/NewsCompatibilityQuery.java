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
    private final ContentCompatibilityExecutor executor;

    public NewsCompatibilityQuery(NewsService legacy, UnifiedLegacyProjectionService unified,
                                  ContentCompatibilityExecutor executor) {
        this.legacy = legacy;
        this.unified = unified;
        this.executor = executor;
    }

    @Override
    public List<NewsArticleResponse> findAll() {
        return executor.execute("NEWS", "list", legacy::findAll, () ->
                unified.findAll("NEWS", "STATIC_NEWS").stream().map(item ->
                        new NewsArticleResponse(item.id(), item.title(), item.category(), item.date(), item.summary()))
                        .toList());
    }

    @Override
    public NewsArticleResponse findById(Long id) {
        NewsArticleResponse legacyItem = legacy.findById(id);
        return executor.execute("NEWS", "detail", () -> legacyItem, () -> {
            var item = unified.findById("NEWS", "STATIC_NEWS", id);
            return new NewsArticleResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        });
    }
}
