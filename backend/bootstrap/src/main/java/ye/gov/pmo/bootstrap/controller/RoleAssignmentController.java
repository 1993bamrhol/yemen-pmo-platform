package ye.gov.pmo.bootstrap.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.identity.dto.RoleAssignmentRequest;
import ye.gov.pmo.identity.dto.RoleAssignmentResponse;
import ye.gov.pmo.identity.service.RoleAssignmentService;
import ye.gov.pmo.organization.service.GovernmentEntityService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin/entities/{entityId}/assignments")
public class RoleAssignmentController {

    private final RoleAssignmentService assignmentService;
    private final GovernmentEntityService entityService;

    public RoleAssignmentController(RoleAssignmentService assignmentService,
                                    GovernmentEntityService entityService) {
        this.assignmentService = assignmentService;
        this.entityService = entityService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('assignments.manage') or @entityAuthorization.canManageAssignments(#p0)")
    public List<RoleAssignmentResponse> findAll(@PathVariable("entityId") UUID entityId) {
        entityService.findEntity(entityId);
        return assignmentService.findForEntity(entityId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('assignments.manage') or @entityAuthorization.canManageAssignments(#p0)")
    public RoleAssignmentResponse grant(@PathVariable("entityId") UUID entityId,
                                        @Valid @RequestBody RoleAssignmentRequest request) {
        entityService.findEntity(entityId);
        return assignmentService.grantEntity(entityId, request);
    }

    @DeleteMapping("/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('assignments.manage') or @entityAuthorization.canManageAssignments(#p0)")
    public void revoke(@PathVariable("entityId") UUID entityId,
                       @PathVariable("assignmentId") UUID assignmentId) {
        entityService.findEntity(entityId);
        assignmentService.revoke(assignmentId, entityId);
    }
}
