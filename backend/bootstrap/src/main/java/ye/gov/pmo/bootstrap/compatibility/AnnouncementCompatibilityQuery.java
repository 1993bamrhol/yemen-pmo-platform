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
    private final ContentCompatibilityExecutor executor;

    public AnnouncementCompatibilityQuery(AnnouncementService legacy, UnifiedLegacyProjectionService unified,
                                          ContentCompatibilityExecutor executor) {
        this.legacy = legacy;
        this.unified = unified;
        this.executor = executor;
    }

    @Override
    public List<AnnouncementResponse> findAll() {
        return executor.execute("ANNOUNCEMENT", "list", legacy::findAll, () ->
                unified.findAll("ANNOUNCEMENT", "STATIC_ANNOUNCEMENTS").stream().map(item ->
                        new AnnouncementResponse(item.id(), item.title(), item.category(), item.date(), item.summary()))
                        .toList());
    }

    @Override
    public AnnouncementResponse findById(Long id) {
        AnnouncementResponse legacyItem = legacy.findById(id);
        return executor.execute("ANNOUNCEMENT", "detail", () -> legacyItem, () -> {
            var item = unified.findById("ANNOUNCEMENT", "STATIC_ANNOUNCEMENTS", id);
            return new AnnouncementResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        });
    }
}
