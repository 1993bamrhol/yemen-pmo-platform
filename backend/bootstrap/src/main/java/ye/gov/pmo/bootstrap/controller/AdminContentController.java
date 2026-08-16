package ye.gov.pmo.bootstrap.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    private final List<ContentItem> content = List.of(
            new ContentItem(1L, "news", "اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية", "منشور", "أحمد علي", "الأخبار", "2026-08-16"),
            new ContentItem(2L, "announcement", "إعلان رسمي عن إطلاق المرحلة الأولى من البوابة الحكومية", "مسودة", "سارة محمد", "الإعلانات", "2026-08-15"),
            new ContentItem(3L, "decision", "قرار اعتماد الهوية البصرية الرسمية", "منشور", "خالد اليماني", "القرارات", "2026-08-12"),
            new ContentItem(4L, "document", "خطة النشر لمرحلة MVP", "مؤرشف", "منى المعلمي", "الوثائق", "2026-08-10"),
            new ContentItem(5L, "news", "تحديثات تنظيمية على مسار النشر", "قيد المراجعة", "علي السامعي", "الأخبار", "2026-08-09"));

    @GetMapping("/content")
    public List<ContentItem> getContent() {
        return content;
    }

    @GetMapping("/content/summary")
    public ContentSummary getSummary() {
        long total = content.size();
        long published = content.stream().filter(item -> "منشور".equals(item.status())).count();
        long draft = content.stream().filter(item -> "مسودة".equals(item.status()) || "قيد المراجعة".equals(item.status())).count();
        long archived = content.stream().filter(item -> "مؤرشف".equals(item.status())).count();
        return new ContentSummary(total, published, draft, archived);
    }

    public record ContentItem(
            Long id,
            String type,
            String title,
            String status,
            String author,
            String category,
            String updatedAt) {
    }

    public record ContentSummary(
            long total,
            long published,
            long draft,
            long archived) {
    }
}
