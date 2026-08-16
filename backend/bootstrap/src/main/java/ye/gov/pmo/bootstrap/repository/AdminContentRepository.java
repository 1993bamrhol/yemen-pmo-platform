package ye.gov.pmo.bootstrap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.bootstrap.entity.ContentItem;

public interface AdminContentRepository extends JpaRepository<ContentItem, Long> {
}
