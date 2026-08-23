package ye.gov.pmo.news.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.news.dto.AnnouncementResponse;
import ye.gov.pmo.news.service.AnnouncementQuery;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementQuery announcementService;

    public AnnouncementController(AnnouncementQuery announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<AnnouncementResponse> findAll() {
        return announcementService.findAll();
    }

    @GetMapping("/{id}")
    public AnnouncementResponse findById(@PathVariable("id") Long id) {
        return announcementService.findById(id);
    }
}
