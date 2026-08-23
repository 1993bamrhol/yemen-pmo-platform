package ye.gov.pmo.content.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ye.gov.pmo.content.domain.ContentStatus;
import ye.gov.pmo.content.domain.ContentType;
import ye.gov.pmo.content.entity.ContentItem;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID>, JpaSpecificationExecutor<ContentItem> {

    boolean existsByContentTypeAndLocaleAndSlug(ContentType contentType, String locale, String slug);

    @EntityGraph(attributePaths = {"currentRevision", "publishedRevision", "primaryEntity", "primaryEntity.entityType"})
    @Query("""
            select item from UnifiedContentItem item
            where item.primaryEntity.id = :entityId
              and (:status is null or item.status = :status)
              and (:contentType is null or item.contentType = :contentType)
            order by item.updatedAt desc, item.id desc
            """)
    Page<ContentItem> findForAdministration(
            @Param("entityId") UUID entityId,
            @Param("status") ContentStatus status,
            @Param("contentType") ContentType contentType,
            Pageable pageable);

    @EntityGraph(attributePaths = {"currentRevision", "publishedRevision", "primaryEntity", "primaryEntity.entityType"})
    @Query("select item from UnifiedContentItem item where item.id = :id")
    Optional<ContentItem> findForAdministrationById(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"publishedRevision", "primaryEntity", "primaryEntity.entityType"})
    Page<ContentItem> findAll(org.springframework.data.jpa.domain.Specification<ContentItem> specification,
                              Pageable pageable);

    @EntityGraph(attributePaths = {"publishedRevision", "primaryEntity", "primaryEntity.entityType"})
    @Query("""
            select item from UnifiedContentItem item
            where item.id = :id and item.publishedRevision is not null and item.archivedAt is null
            """)
    Optional<ContentItem> findPublicById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"publishedRevision", "primaryEntity", "primaryEntity.entityType"})
    @Query("""
            select item from UnifiedContentItem item
            where item.contentType = :contentType and item.locale = :locale and item.slug = :slug
              and item.publishedRevision is not null and item.archivedAt is null
            """)
    Optional<ContentItem> findPublicBySlug(
            @Param("contentType") ContentType contentType,
            @Param("locale") String locale,
            @Param("slug") String slug);
}
