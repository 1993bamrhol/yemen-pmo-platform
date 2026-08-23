package ye.gov.pmo.content.controller;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.content.dto.PageResponse;
import ye.gov.pmo.content.dto.PublicContentResponse;
import ye.gov.pmo.content.service.PublicContentService;

@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix = "features.unified-content-read", name = "enabled", havingValue = "true")
public class PublicContentController {

    private final PublicContentService service;

    public PublicContentController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/content")
    public PageResponse<PublicContentResponse> findAll(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "entityId", required = false) UUID entityId,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.findPublished(type, entityId, category, dateFrom, dateTo, page, size);
    }

    @GetMapping("/content/{id}")
    public PublicContentResponse findById(@PathVariable("id") UUID id) {
        return service.findById(id);
    }

    @GetMapping("/content/by-slug/{type}/{slug}")
    public PublicContentResponse findBySlug(
            @PathVariable("type") String type, @PathVariable("slug") String slug) {
        return service.findBySlug(type, slug);
    }

    @GetMapping("/entities/{entityId}/content")
    public PageResponse<PublicContentResponse> findForEntity(
            @PathVariable("entityId") UUID entityId,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.findPublished(type, entityId, category, dateFrom, dateTo, page, size);
    }
}
