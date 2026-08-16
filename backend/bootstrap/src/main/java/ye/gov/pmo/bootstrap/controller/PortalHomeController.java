package ye.gov.pmo.bootstrap.controller;

import java.util.List;
import ye.gov.pmo.decisions.dto.DecisionResponse;
import ye.gov.pmo.decisions.service.DecisionService;
import ye.gov.pmo.documents.dto.DocumentResponse;
import ye.gov.pmo.documents.service.DocumentService;
import ye.gov.pmo.news.dto.NewsArticleResponse;
import ye.gov.pmo.news.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal")
public class PortalHomeController {

    private final NewsService newsService;
    private final DecisionService decisionService;
    private final DocumentService documentService;

    public PortalHomeController(NewsService newsService, DecisionService decisionService, DocumentService documentService) {
        this.newsService = newsService;
        this.decisionService = decisionService;
        this.documentService = documentService;
    }

    @GetMapping("/home")
    public PortalHomeResponse home() {
        return new PortalHomeResponse(
                new Hero(
                        "بوابة رئاسة مجلس الوزراء اليمني: المصدر الرسمي للمعلومة الحكومية",
                        "منصة سيادية حديثة تجمع الأخبار والبيانات والقرارات والخدمات في واجهة عربية واضحة تعكس الهوية اليمنية الرسمية.",
                        "استعراض المحتوى الرسمي",
                        "الانتقال إلى الأخبار"),
                List.of(
                        new Metric("أخبار حديثة", "24"),
                        new Metric("بيانات رسمية", "11"),
                        new Metric("قرارات", "8"),
                        new Metric("وثائق", "35")),
                List.of(
                        "المصدر الرسمي للمعلومة الحكومية",
                        "واجهة عربية أولًا",
                        "أرشفة منظمة وقابلة للبحث",
                        "خدمات عامة موثوقة"),
                List.of(
                        new Channel("البريد الرسمي", "info@example.gov.ye"),
                        new Channel("مركز الاتصال", "+967 1 000 000"),
                        new Channel("ساعات الخدمة", "الأحد - الخميس")),
                newsService.findAll(),
                List.of(
                        new ContentCard(
                                "بيان رسمي حول تقدم أعمال البوابة",
                                "بيان رسمي",
                                "التأكيد على أن البوابة ستكون المصدر الرسمي للمعلومة الحكومية."),
                        new ContentCard(
                                "تحديثات تنظيمية على مسار النشر",
                                "أمانة عامة",
                                "ضبط إجراءات النشر والمراجعة والصلاحيات التحريرية.")),
                decisionService.findAll().stream()
                        .map(decision -> new ContentCard(
                                decision.title(),
                                decision.category(),
                                decision.description()))
                        .toList(),
                List.of(
                        new ServiceCard(
                                "تواصل رسمي",
                                "قنوات واضحة لتلقي الاستفسارات والملاحظات والاقتراحات."),
                        new ServiceCard(
                                "الوثائق والتحميلات",
                                "وصول مباشر إلى الملفات الرسمية والقرارات والأدلة."),
                        new ServiceCard(
                                "الأسئلة الشائعة",
                                "إجابات سريعة حول الخدمات والمحتوى وآلية الاستخدام.")),
                List.of(
                        "إرسال استفسار",
                        "تقديم اقتراح",
                        "تحميل الوثائق",
                        "الأسئلة الشائعة"),
                documentService.findAll().stream()
                        .map(DocumentResponse::title)
                        .toList(),
                List.of("صور رسمية", "فيديوهات", "تصريحات", "تغطيات"),
                List.of("الشفافية", "السرعة", "الدقة", "الموثوقية", "إمكانية الوصول"));
    }

    public record PortalHomeResponse(
            Hero hero,
            List<Metric> stats,
            List<String> portalHighlights,
            List<Channel> officialChannels,
            List<NewsArticleResponse> latestNews,
            List<ContentCard> officialStatements,
            List<ContentCard> decisions,
            List<ServiceCard> serviceCards,
            List<String> services,
            List<String> documents,
            List<String> mediaItems,
            List<String> governancePrinciples) {
    }

    public record Hero(String title, String description, String ctaLabel, String secondaryCtaLabel) {
    }

    public record Metric(String label, String value) {
    }

    public record Channel(String label, String value) {
    }

    public record ContentCard(String title, String meta, String description) {
    }

    public record ServiceCard(String title, String description) {
    }
}
