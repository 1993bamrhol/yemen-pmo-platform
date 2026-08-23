package ye.gov.pmo.organization.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.organization.dto.EntityTypeResponse;
import ye.gov.pmo.organization.dto.EntityRelationshipRequest;
import ye.gov.pmo.organization.dto.EntityRelationshipResponse;
import ye.gov.pmo.organization.dto.GovernmentEntityRequest;
import ye.gov.pmo.organization.dto.GovernmentEntityResponse;
import ye.gov.pmo.organization.service.GovernmentEntityService;
import ye.gov.pmo.organization.service.EntityRelationshipService;

@RestController
@RequestMapping("/api/v1")
public class GovernmentEntityController {

    private final GovernmentEntityService service;
    private final EntityRelationshipService relationshipService;

    public GovernmentEntityController(GovernmentEntityService service,
                                      EntityRelationshipService relationshipService) {
        this.service = service;
        this.relationshipService = relationshipService;
    }

    @GetMapping("/entity-types")
    public List<EntityTypeResponse> findTypes() {
        return service.findActiveTypes();
    }

    @GetMapping("/entities")
    public List<GovernmentEntityResponse> findAll() {
        return service.findPublicEntities();
    }

    @GetMapping("/entities/{id}")
    public GovernmentEntityResponse findById(@PathVariable("id") UUID id) {
        return service.findPublicById(id);
    }

    @GetMapping("/entities/by-slug/{type}/{slug}")
    public GovernmentEntityResponse findBySlug(@PathVariable("type") String type,
                                               @PathVariable("slug") String slug) {
        return service.findPublicBySlug(type, slug);
    }

    @GetMapping("/entities/{id}/children")
    public List<GovernmentEntityResponse> findChildren(@PathVariable("id") UUID id) {
        return service.findPublicChildren(id);
    }

    @PostMapping("/admin/entities")
    @PreAuthorize("hasAuthority('entities.manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public GovernmentEntityResponse create(@Valid @RequestBody GovernmentEntityRequest request) {
        return service.create(request);
    }

    @PutMapping("/admin/entities/{id}")
    @PreAuthorize("hasAuthority('entities.manage') or @entityAuthorization.canManage(#p0)")
    public GovernmentEntityResponse update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody GovernmentEntityRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/admin/entities/{id}/relationships")
    @PreAuthorize("hasAuthority('entities.manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityRelationshipResponse createRelationship(
            @PathVariable("id") UUID id, @Valid @RequestBody EntityRelationshipRequest request) {
        return relationshipService.create(id, request);
    }
}
