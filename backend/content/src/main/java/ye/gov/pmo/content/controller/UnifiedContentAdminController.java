package ye.gov.pmo.content.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.content.domain.ContentStatus;
import ye.gov.pmo.content.domain.ContentType;
import ye.gov.pmo.content.dto.AdminContentResponse;
import ye.gov.pmo.content.dto.ContentCreateRequest;
import ye.gov.pmo.content.dto.ContentRevisionRequest;
import ye.gov.pmo.content.dto.ContentTransitionRequest;
import ye.gov.pmo.content.dto.ContentTransitionResponse;
import ye.gov.pmo.content.dto.EditorialVerificationRequest;
import ye.gov.pmo.content.dto.PageResponse;
import ye.gov.pmo.content.service.AdminContentService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin")
public class UnifiedContentAdminController {
    private final AdminContentService service;

    public UnifiedContentAdminController(AdminContentService service) {
        this.service = service;
    }

    @GetMapping("/entities/{entityId}/content")
    @PreAuthorize("@entityAuthorization.hasPermission(#p0, 'content.read', 'content.manage')")
    public PageResponse<AdminContentResponse> findForEntity(
            @PathVariable("entityId") UUID entityId,
            @RequestParam(name = "status", required = false) ContentStatus status,
            @RequestParam(name = "type", required = false) ContentType type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.findForEntity(entityId, status, type, page, size);
    }

    @PostMapping("/entities/{entityId}/content")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@entityAuthorization.hasPermission(#p0, 'content.write', 'content.manage')")
    public AdminContentResponse create(@PathVariable("entityId") UUID entityId,
                                       @Valid @RequestBody ContentCreateRequest request) {
        return service.create(entityId, request);
    }

    @GetMapping("/content/{contentId}")
    public AdminContentResponse findById(@PathVariable("contentId") UUID contentId) {
        return service.findById(contentId);
    }

    @PostMapping("/content/{contentId}/revisions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminContentResponse createRevision(
            @PathVariable("contentId") UUID contentId,
            @Valid @RequestBody ContentRevisionRequest request) {
        return service.createRevision(contentId, request);
    }

    @PostMapping("/content/{contentId}/transitions")
    public ContentTransitionResponse transition(
            @PathVariable("contentId") UUID contentId,
            @Valid @RequestBody ContentTransitionRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.transition(contentId, request, correlationId);
    }

    @PutMapping("/content/{contentId}/editorial-verification")
    public AdminContentResponse updateEditorialVerification(
            @PathVariable("contentId") UUID contentId,
            @Valid @RequestBody EditorialVerificationRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.updateEditorialVerification(contentId, request, correlationId);
    }
}
