package ye.gov.pmo.content.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ye.gov.pmo.content.entity.ContentRevision;

public interface ContentRevisionRepository extends JpaRepository<ContentRevision, UUID> {
    @Query("select coalesce(max(r.revisionNumber), 0) from ContentRevision r where r.contentItem.id = :itemId")
    int findMaxRevisionNumber(@Param("itemId") UUID itemId);
}
