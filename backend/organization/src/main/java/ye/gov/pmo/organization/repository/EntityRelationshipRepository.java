package ye.gov.pmo.organization.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.organization.entity.EntityRelationship;
import ye.gov.pmo.organization.entity.EntityRelationshipType;

public interface EntityRelationshipRepository extends JpaRepository<EntityRelationship, UUID> {
    boolean existsBySourceIdAndTargetIdAndRelationshipType(
            UUID sourceId, UUID targetId, EntityRelationshipType relationshipType);
}
