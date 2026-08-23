package ye.gov.pmo.organization.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.organization.entity.EntityType;

public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
    Optional<EntityType> findByCode(String code);
    Optional<EntityType> findByPublicPathSegment(String publicPathSegment);
}
