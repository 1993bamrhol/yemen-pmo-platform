package ye.gov.pmo.news.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.news.dto.AnnouncementResponse;

@Service
public class AnnouncementService implements AnnouncementQuery {

    private final List<AnnouncementResponse> announcements = List.of(
            new AnnouncementResponse(
                    1L,
                    "إعلان رسمي عن إطلاق المرحلة الأولى من البوابة الحكومية",
                    "إعلان رسمي",
                    "18 أغسطس 2026",
                    "تبدأ الرئاسة في نشر البيانات الرسمية والخدمات الأساسية عبر البوابة الموحدة."),
            new AnnouncementResponse(
                    2L,
                    "تحديث نظام الاستقبال الإلكتروني للملاحظات والاقتراحات",
                    "خدمة عامة",
                    "17 أغسطس 2026",
                    "يتم توحيد قنوات الاستقبال ومراجعة الطلبات بطريقة موحدة وشفافة."),
            new AnnouncementResponse(
                    3L,
                    "إعلان حول آلية نشر البيانات والوثائق الرسمية",
                    "إرشاد",
                    "16 أغسطس 2026",
                    "يحدد الإعلان أوقات النشر ومراجعة المحتوى وتحديث الوثائق الرسمية."));

    @Override
    public List<AnnouncementResponse> findAll() {
        return announcements;
    }

    @Override
    public AnnouncementResponse findById(Long id) {
        return announcements.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Announcement not found with id: " + id));
    }
}
