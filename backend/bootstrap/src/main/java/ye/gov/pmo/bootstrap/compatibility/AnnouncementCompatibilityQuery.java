package ye.gov.pmo.bootstrap.compatibility;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ye.gov.pmo.news.dto.AnnouncementResponse;
import ye.gov.pmo.news.service.AnnouncementQuery;
import ye.gov.pmo.news.service.AnnouncementService;

@Service
@Primary
public class AnnouncementCompatibilityQuery implements AnnouncementQuery {
    private final AnnouncementService legacy;
    private final UnifiedLegacyProjectionService unified;
    private final ContentCompatibilityRouter router;

    public AnnouncementCompatibilityQuery(AnnouncementService legacy, UnifiedLegacyProjectionService unified,
                                          ContentCompatibilityRouter router) {
        this.legacy = legacy;
        this.unified = unified;
        this.router = router;
    }

    @Override
    public List<AnnouncementResponse> findAll() {
        if (!router.useUnified("ANNOUNCEMENT")) return legacy.findAll();
        try {
            return unified.findAll("ANNOUNCEMENT", "STATIC_ANNOUNCEMENTS").stream().map(item ->
                    new AnnouncementResponse(item.id(), item.title(), item.category(), item.date(), item.summary())).toList();
        } catch (RuntimeException exception) {
            return legacy.findAll();
        }
    }

    @Override
    public AnnouncementResponse findById(Long id) {
        if (!router.useUnified("ANNOUNCEMENT")) return legacy.findById(id);
        try {
            var item = unified.findById("ANNOUNCEMENT", "STATIC_ANNOUNCEMENTS", id);
            return new AnnouncementResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        } catch (RuntimeException exception) {
            return legacy.findById(id);
        }
    }
}
