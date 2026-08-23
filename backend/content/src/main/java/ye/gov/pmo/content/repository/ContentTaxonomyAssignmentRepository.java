package ye.gov.pmo.content.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ye.gov.pmo.content.entity.ContentTaxonomyAssignment;
import ye.gov.pmo.content.entity.ContentTaxonomyAssignmentId;

public interface ContentTaxonomyAssignmentRepository
        extends JpaRepository<ContentTaxonomyAssignment, ContentTaxonomyAssignmentId> {

    @EntityGraph(attributePaths = "term")
    @Query("""
            select assignment from ContentTaxonomyAssignment assignment
            where assignment.contentItem.id in :contentIds
              and assignment.term.taxonomyCode = 'CONTENT_CATEGORY'
              and assignment.term.active = true
            """)
    List<ContentTaxonomyAssignment> findActiveCategoriesForContent(
            @Param("contentIds") Collection<UUID> contentIds);
}
