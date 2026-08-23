package ye.gov.pmo.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.identity.entity.RoleAssignment;
import ye.gov.pmo.identity.entity.ScopeType;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    List<RoleAssignment> findAllByUserIdAndEnabledTrue(Long userId);
    List<RoleAssignment> findAllByGovernmentEntityIdAndEnabledTrue(UUID governmentEntityId);
    Optional<RoleAssignment> findByUserIdAndRoleIdAndScopeTypeAndGovernmentEntityId(
            Long userId, Long roleId, ScopeType scopeType, UUID governmentEntityId);
    Optional<RoleAssignment> findByUserIdAndRoleIdAndScopeTypeAndGovernmentEntityIdIsNull(
            Long userId, Long roleId, ScopeType scopeType);
}
