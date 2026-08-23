package ye.gov.pmo.identity.security;

import java.util.UUID;
import org.springframework.stereotype.Component;
import ye.gov.pmo.identity.entity.RoleAssignment;
import ye.gov.pmo.identity.entity.ScopeType;
import ye.gov.pmo.identity.repository.UserRepository;
import ye.gov.pmo.identity.service.RoleAssignmentService;
import ye.gov.pmo.shared.security.CurrentActorProvider;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;

@Component("entityAuthorization")
public class EntityAuthorization {

    private final CurrentActorProvider actorProvider;
    private final UserRepository userRepository;
    private final RoleAssignmentService assignmentService;
    private final AuditService auditService;

    public EntityAuthorization(CurrentActorProvider actorProvider, UserRepository userRepository,
                               RoleAssignmentService assignmentService, AuditService auditService) {
        this.actorProvider = actorProvider;
        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
        this.auditService = auditService;
    }

    public boolean canManage(UUID entityId) {
        return hasPermission(entityId, "entities.manage", "entities.write");
    }

    public boolean canManageAssignments(UUID entityId) {
        return hasPermission(entityId, "assignments.manage", "assignments.write");
    }

    public boolean hasPermission(UUID entityId, String... permissions) {
        String username = actorProvider.currentUsername();
        if (username == null) {
            return false;
        }
        boolean allowed = userRepository.findByUsername(username)
                .map(user -> assignmentService.findActiveForUser(user.getId()).stream()
                        .filter(assignment -> appliesTo(assignment, entityId))
                        .flatMap(assignment -> assignment.getRole().getPermissions().stream())
                        .anyMatch(permission -> matches(permission.getName(), permissions)))
                .orElse(false);
        if (!allowed) {
            auditService.recordIndependent(actorProvider.currentUserId(), "ENTITY_ACCESS_DENIED", "GovernmentEntity",
                    entityId.toString(), entityId, AuditOutcome.DENIED, null,
                    "requiredPermission=" + String.join("|", permissions));
        }
        return allowed;
    }

    public boolean hasPlatformPermission(String... permissions) {
        String username = actorProvider.currentUsername();
        if (username == null) return false;
        boolean allowed = userRepository.findByUsername(username)
                .map(user -> assignmentService.findActiveForUser(user.getId()).stream()
                        .filter(assignment -> assignment.getScopeType() == ScopeType.PLATFORM)
                        .flatMap(assignment -> assignment.getRole().getPermissions().stream())
                        .anyMatch(permission -> matches(permission.getName(), permissions)))
                .orElse(false);
        if (!allowed) {
            auditService.recordIndependent(actorProvider.currentUserId(), "PLATFORM_ACCESS_DENIED", "Platform",
                    null, null, AuditOutcome.DENIED, null,
                    "requiredPermission=" + String.join("|", permissions));
        }
        return allowed;
    }

    private boolean appliesTo(RoleAssignment assignment, UUID entityId) {
        return assignment.getScopeType() == ScopeType.PLATFORM
                || (assignment.getScopeType() == ScopeType.ENTITY
                    && entityId.equals(assignment.getGovernmentEntityId()));
    }

    private boolean matches(String value, String[] permissions) {
        for (String permission : permissions) {
            if (permission.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
