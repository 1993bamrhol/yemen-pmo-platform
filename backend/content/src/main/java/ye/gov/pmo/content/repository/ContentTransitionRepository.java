package ye.gov.pmo.content.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.content.entity.ContentTransition;

public interface ContentTransitionRepository extends JpaRepository<ContentTransition, UUID> {
    List<ContentTransition> findAllByContentItemIdOrderByOccurredAtAsc(UUID contentItemId);
}
