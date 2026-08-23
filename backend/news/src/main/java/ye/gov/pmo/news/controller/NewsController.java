package ye.gov.pmo.news.controller;

import java.util.List;
import ye.gov.pmo.news.dto.NewsArticleResponse;
import ye.gov.pmo.news.service.NewsQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsQuery newsService;

    public NewsController(NewsQuery newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsArticleResponse> findAll() {
        return newsService.findAll();
    }

    @GetMapping("/{id}")
    public NewsArticleResponse findById(@PathVariable("id") Long id) {
        return newsService.findById(id);
    }
}
