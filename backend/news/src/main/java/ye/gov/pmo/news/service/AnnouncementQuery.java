package ye.gov.pmo.news.service;

import java.util.List;
import ye.gov.pmo.news.dto.AnnouncementResponse;

public interface AnnouncementQuery {
    List<AnnouncementResponse> findAll();

    AnnouncementResponse findById(Long id);
}
