package ye.gov.pmo.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ye.gov.pmo.organization.entity.EntityStatus;
import ye.gov.pmo.organization.entity.GovernmentEntity;

public interface GovernmentEntityRepository
        extends JpaRepository<GovernmentEntity, UUID>, JpaSpecificationExecutor<GovernmentEntity> {
    List<GovernmentEntity> findAllByStatusOrderByOfficialNameArAsc(EntityStatus status);
    List<GovernmentEntity> findAllByParentIdAndStatusOrderByOfficialNameArAsc(UUID parentId, EntityStatus status);
    Optional<GovernmentEntity> findByEntityTypeIdAndSlug(Long entityTypeId, String slug);
    boolean existsByEntityTypeIdAndSlugAndIdNot(Long entityTypeId, String slug, UUID id);
    boolean existsByEntityTypeIdAndSlug(Long entityTypeId, String slug);

    @Override
    @EntityGraph(attributePaths = {"entityType", "parent", "parent.entityType"})
    Page<GovernmentEntity> findAll(Specification<GovernmentEntity> specification, Pageable pageable);
}
