package ye.gov.pmo.organization.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ye.gov.pmo.organization.entity.EntityType;

public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
    Optional<EntityType> findByCode(String code);
    Optional<EntityType> findByPublicPathSegment(String publicPathSegment);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select type from EntityType type where type.id in :ids order by type.id")
    List<EntityType> lockAllByIdInOrder(@Param("ids") Collection<Long> ids);
}
