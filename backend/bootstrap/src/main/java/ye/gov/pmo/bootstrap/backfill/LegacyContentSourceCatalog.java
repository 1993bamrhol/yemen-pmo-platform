package ye.gov.pmo.bootstrap.backfill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ye.gov.pmo.bootstrap.repository.AdminContentRepository;
import ye.gov.pmo.decisions.service.DecisionService;
import ye.gov.pmo.documents.service.DocumentService;
import ye.gov.pmo.news.service.AnnouncementService;
import ye.gov.pmo.news.service.NewsService;

@Component
public class LegacyContentSourceCatalog {
    private static final Map<String, String> ADMIN_STATUSES = Map.of(
            "منشور", "PUBLISHED",
            "مسودة", "DRAFT",
            "قيد المراجعة", "IN_REVIEW",
            "مؤرشف", "ARCHIVED");

    private final AdminContentRepository adminContent;
    private final NewsService news;
    private final AnnouncementService announcements;
    private final DecisionService decisions;
    private final DocumentService documents;

    public LegacyContentSourceCatalog(AdminContentRepository adminContent, NewsService news,
                                      AnnouncementService announcements, DecisionService decisions,
                                      DocumentService documents) {
        this.adminContent = adminContent;
        this.news = news;
        this.announcements = announcements;
        this.decisions = decisions;
        this.documents = documents;
    }

    public List<Snapshot> discover() {
        List<Snapshot> sources = new ArrayList<>();
        adminContent.findAll().forEach(item -> sources.add(new Snapshot(
                "ADMIN_CONTENT", item.getType().toUpperCase(), item.getId(), item.getTitle(),
                ADMIN_STATUSES.getOrDefault(item.getStatus(), "UNKNOWN:" + item.getStatus()),
                item.getCategory(), item.getAuthor(), item.getUpdatedAt().toString(), null)));
        news.findAll().forEach(item -> sources.add(new Snapshot(
                "STATIC_NEWS", "NEWS", item.id(), item.title(), "PUBLISHED",
                item.category(), null, item.date(), item.excerpt())));
        announcements.findAll().forEach(item -> sources.add(new Snapshot(
                "STATIC_ANNOUNCEMENTS", "ANNOUNCEMENT", item.id(), item.title(), "PUBLISHED",
                item.category(), null, item.date(), item.excerpt())));
        decisions.findAll().forEach(item -> sources.add(new Snapshot(
                "STATIC_DECISIONS", "DECISION", item.id(), item.title(), "PUBLISHED",
                item.category(), null, item.date(), item.description())));
        documents.findAll().forEach(item -> sources.add(new Snapshot(
                "STATIC_DOCUMENTS", "DOCUMENT", item.id(), item.title(), "PUBLISHED",
                item.category(), null, item.updatedAt(), item.description())));
        return sources.stream().sorted(Comparator.comparing(Snapshot::sourceKey)).toList();
    }

    public record Snapshot(
            String sourceSystem,
            String sourceType,
            long legacyId,
            String title,
            String status,
            String category,
            String byline,
            String sourceDate,
            String summary) {

        public String sourceKey() {
            return sourceSystem + ":" + sourceType + ":" + legacyId;
        }

        public boolean suppliesBody() {
            return summary != null && !summary.isBlank();
        }
    }
}
