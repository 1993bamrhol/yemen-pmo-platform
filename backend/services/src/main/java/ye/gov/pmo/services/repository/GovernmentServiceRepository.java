package ye.gov.pmo.services.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ye.gov.pmo.services.entity.GovernmentService;

public interface GovernmentServiceRepository
        extends JpaRepository<GovernmentService, UUID>, JpaSpecificationExecutor<GovernmentService> {

    @EntityGraph(attributePaths = {"owningEntity", "owningEntity.entityType"})
    Optional<GovernmentService> findById(UUID id);

    @EntityGraph(attributePaths = {"owningEntity", "owningEntity.entityType"})
    Optional<GovernmentService> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
