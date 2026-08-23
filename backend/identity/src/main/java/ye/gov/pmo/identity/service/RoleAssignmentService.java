package ye.gov.pmo.identity.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.identity.dto.RoleAssignmentRequest;
import ye.gov.pmo.identity.dto.RoleAssignmentResponse;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.RoleAssignment;
import ye.gov.pmo.identity.entity.ScopeType;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.repository.RoleAssignmentRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
@Transactional(readOnly = true)
public class RoleAssignmentService {

    private static final Set<String> ENTITY_ASSIGNABLE_ROLES = Set.of(
            "PMO_ADMIN", "ENTITY_ADMIN", "EDITOR", "REVIEWER", "PUBLISHER", "SERVICE_MANAGER");

    private final RoleAssignmentRepository repository;
    private final UserService userService;
    private final RoleService roleService;
    private final CurrentActorProvider actorProvider;
    private final AuditService auditService;

    public RoleAssignmentService(RoleAssignmentRepository repository, UserService userService,
                                 RoleService roleService, CurrentActorProvider actorProvider,
                                 AuditService auditService) {
        this.repository = repository;
        this.userService = userService;
        this.roleService = roleService;
        this.actorProvider = actorProvider;
        this.auditService = auditService;
    }

    public List<RoleAssignmentResponse> findForEntity(UUID entityId) {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.findAllByGovernmentEntityIdAndEnabledTrue(entityId).stream()
                .filter(item -> item.isActiveAt(now))
                .map(this::toResponse)
                .toList();
    }

    public List<RoleAssignment> findActiveForUser(Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.findAllByUserIdAndEnabledTrue(userId).stream()
                .filter(item -> item.isActiveAt(now))
                .toList();
    }

    @Transactional
    public RoleAssignmentResponse grantEntity(UUID entityId, RoleAssignmentRequest request) {
        validateDates(request.validFrom(), request.validUntil());
        User user = userService.findById(request.userId());
        Role role = roleService.findById(request.roleId());
        enforceGrantBoundary(role);
        Long actorId = actorProvider.currentUserId();
        RoleAssignment assignment = repository
                .findByUserIdAndRoleIdAndScopeTypeAndGovernmentEntityId(
                        user.getId(), role.getId(), ScopeType.ENTITY, entityId)
                .map(existing -> {
                    existing.reactivate(request.validFrom(), request.validUntil(), actorId);
                    return existing;
                })
                .orElseGet(() -> new RoleAssignment(
                        user, role, ScopeType.ENTITY, entityId,
                        request.validFrom(), request.validUntil(), actorId));
        RoleAssignment saved = repository.save(assignment);
        auditService.record(actorId, "ROLE_ASSIGNMENT_GRANTED", "RoleAssignment", saved.getId().toString(),
                entityId, AuditOutcome.SUCCESS, null, "role=" + role.getName() + ";user=" + user.getUsername());
        return toResponse(saved);
    }

    @Transactional
    public RoleAssignmentResponse grantPlatform(Long userId, Long roleId, Long grantedBy) {
        User user = userService.findById(userId);
        Role role = roleService.findById(roleId);
        RoleAssignment assignment = repository
                .findByUserIdAndRoleIdAndScopeTypeAndGovernmentEntityIdIsNull(userId, roleId, ScopeType.PLATFORM)
                .map(existing -> {
                    existing.reactivate(null, null, grantedBy);
                    return existing;
                })
                .orElseGet(() -> new RoleAssignment(user, role, ScopeType.PLATFORM, null, null, null, grantedBy));
        return toResponse(repository.save(assignment));
    }

    @Transactional
    public RoleAssignmentResponse grantEntityForBootstrap(Long userId, Long roleId, UUID entityId, Long grantedBy) {
        User user = userService.findById(userId);
        Role role = roleService.findById(roleId);
        RoleAssignment assignment = repository
                .findByUserIdAndRoleIdAndScopeTypeAndGovernmentEntityId(userId, roleId, ScopeType.ENTITY, entityId)
                .map(existing -> {
                    existing.reactivate(null, null, grantedBy);
                    return existing;
                })
                .orElseGet(() -> new RoleAssignment(user, role, ScopeType.ENTITY, entityId, null, null, grantedBy));
        return toResponse(repository.save(assignment));
    }

    @Transactional
    public void revoke(UUID assignmentId, UUID expectedEntityId) {
        RoleAssignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role assignment not found"));
        if (assignment.getScopeType() != ScopeType.ENTITY
                || !expectedEntityId.equals(assignment.getGovernmentEntityId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role assignment not found");
        }
        if (assignment.getUser().getId().equals(actorProvider.currentUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users cannot revoke their own assignment");
        }
        assignment.disable();
        repository.save(assignment);
        auditService.record(actorProvider.currentUserId(), "ROLE_ASSIGNMENT_REVOKED", "RoleAssignment",
                assignment.getId().toString(), expectedEntityId, AuditOutcome.SUCCESS, null,
                "role=" + assignment.getRole().getName() + ";user=" + assignment.getUser().getUsername());
    }

    private void enforceGrantBoundary(Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean platformManager = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "assignments.manage".equals(authority.getAuthority()));
        if (!platformManager && !ENTITY_ASSIGNABLE_ROLES.contains(role.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This role cannot be granted by an entity administrator");
        }
    }

    private void validateDates(OffsetDateTime validFrom, OffsetDateTime validUntil) {
        if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil must not be before validFrom");
        }
    }

    private RoleAssignmentResponse toResponse(RoleAssignment assignment) {
        return new RoleAssignmentResponse(
                assignment.getId(),
                assignment.getUser().getId(),
                assignment.getUser().getUsername(),
                assignment.getRole().getId(),
                assignment.getRole().getName(),
                assignment.getScopeType().name(),
                assignment.getGovernmentEntityId(),
                assignment.getValidFrom(),
                assignment.getValidUntil(),
                assignment.isEnabled(),
                assignment.getCreatedAt());
    }
}
