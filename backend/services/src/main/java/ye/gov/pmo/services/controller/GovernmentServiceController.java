package ye.gov.pmo.services.controller;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.services.dto.ServiceDetailResponse;
import ye.gov.pmo.services.dto.ServiceDirectoryResponse;
import ye.gov.pmo.services.service.GovernmentServiceService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1")
public class GovernmentServiceController {

    private final GovernmentServiceService service;

    public GovernmentServiceController(GovernmentServiceService service) {
        this.service = service;
    }

    @GetMapping("/services")
    public ServiceDirectoryResponse findAll(
            @RequestParam(name = "entityId", required = false) UUID entityId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.findPublicServices(entityId, page, size);
    }

    @GetMapping("/services/{id}")
    public ServiceDetailResponse findById(@PathVariable("id") UUID id) {
        return service.findPublicById(id);
    }

    @GetMapping("/services/by-slug/{slug}")
    public ServiceDetailResponse findBySlug(@PathVariable("slug") String slug) {
        return service.findPublicBySlug(slug);
    }

    @GetMapping("/entities/{entityId}/services")
    public ServiceDirectoryResponse findForEntity(
            @PathVariable("entityId") UUID entityId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.findPublicServices(entityId, page, size);
    }
}
