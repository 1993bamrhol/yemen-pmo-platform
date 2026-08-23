package ye.gov.pmo.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.organization.entity.EntityStatus;
import ye.gov.pmo.organization.entity.GovernmentEntity;

public interface GovernmentEntityRepository extends JpaRepository<GovernmentEntity, UUID> {
    List<GovernmentEntity> findAllByStatusOrderByOfficialNameArAsc(EntityStatus status);
    List<GovernmentEntity> findAllByParentIdAndStatusOrderByOfficialNameArAsc(UUID parentId, EntityStatus status);
    Optional<GovernmentEntity> findByEntityTypeIdAndSlug(Long entityTypeId, String slug);
    boolean existsByEntityTypeIdAndSlugAndIdNot(Long entityTypeId, String slug, UUID id);
    boolean existsByEntityTypeIdAndSlug(Long entityTypeId, String slug);
}
