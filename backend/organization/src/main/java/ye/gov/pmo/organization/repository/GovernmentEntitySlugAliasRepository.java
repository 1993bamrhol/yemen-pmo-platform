package ye.gov.pmo.organization.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.organization.entity.GovernmentEntitySlugAlias;

public interface GovernmentEntitySlugAliasRepository
        extends JpaRepository<GovernmentEntitySlugAlias, UUID> {

    @EntityGraph(attributePaths = {
            "governmentEntity", "governmentEntity.entityType", "governmentEntity.parent",
            "governmentEntity.parent.entityType"
    })
    Optional<GovernmentEntitySlugAlias> findByPublicPathSegmentAndSlug(
            String publicPathSegment, String slug);
}
