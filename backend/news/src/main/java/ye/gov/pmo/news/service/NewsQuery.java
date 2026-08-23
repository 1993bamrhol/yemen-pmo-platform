package ye.gov.pmo.news.service;

import java.util.List;
import ye.gov.pmo.news.dto.NewsArticleResponse;

public interface NewsQuery {
    List<NewsArticleResponse> findAll();

    NewsArticleResponse findById(Long id);
}
