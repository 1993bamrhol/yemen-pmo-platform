package ye.gov.pmo.content.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.content.domain.ContentType;
import ye.gov.pmo.content.dto.PageResponse;
import ye.gov.pmo.content.dto.PublicContentResponse;
import ye.gov.pmo.content.entity.ContentItem;
import ye.gov.pmo.content.entity.ContentRevision;
import ye.gov.pmo.content.entity.ContentTaxonomyAssignment;
import ye.gov.pmo.content.entity.TaxonomyTerm;
import ye.gov.pmo.content.repository.ContentItemRepository;
import ye.gov.pmo.content.repository.ContentTaxonomyAssignmentRepository;
import ye.gov.pmo.organization.entity.EntityType;
import ye.gov.pmo.organization.entity.GovernmentEntity;

@Service
@Transactional(readOnly = true)
public class PublicContentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ContentItemRepository contentRepository;
    private final ContentTaxonomyAssignmentRepository taxonomyRepository;

    public PublicContentService(ContentItemRepository contentRepository,
                                ContentTaxonomyAssignmentRepository taxonomyRepository) {
        this.contentRepository = contentRepository;
        this.taxonomyRepository = taxonomyRepository;
    }

    public PageResponse<PublicContentResponse> findPublished(
            String type, UUID entityId, String category, LocalDate dateFrom, LocalDate dateTo,
            int page, int size) {
        return findPublished(type, entityId, category, dateFrom, dateTo, page, size, true);
    }

    public PageResponse<PublicContentResponse> findPublishedForCompatibility(
            String type, int page, int size) {
        return findPublished(type, null, null, null, null, page, size, false);
    }

    private PageResponse<PublicContentResponse> findPublished(
            String type, UUID entityId, String category, LocalDate dateFrom, LocalDate dateTo,
            int page, int size, boolean requireEditorialVerification) {
        validatePage(page, size);
        if (dateFrom != null && dateTo != null && dateTo.isBefore(dateFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateTo must not be before dateFrom");
        }
        ContentType contentType = parseType(type);
        String categorySlug = normalizeCategory(category);
        OffsetDateTime from = dateFrom == null ? null : dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toExclusive = dateTo == null
                ? null
                : dateTo.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        Page<ContentItem> result = contentRepository.findAll(
                publishedSpecification(contentType, entityId, categorySlug, from, toExclusive,
                        requireEditorialVerification),
                PageRequest.of(page, size, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("lastPublishedAt"),
                        org.springframework.data.domain.Sort.Order.desc("id"))));
        Map<UUID, List<PublicContentResponse.CategoryReference>> categories = categoriesFor(result.getContent());
        List<PublicContentResponse> items = result.getContent().stream()
                .map(item -> toResponse(item, categories.getOrDefault(item.getId(), List.of())))
                .toList();
        return PageResponse.from(result, items);
    }

    public PublicContentResponse findById(UUID id) {
        ContentItem item = contentRepository.findPublicById(id)
                .orElseThrow(() -> notFound());
        return toResponse(item, categoriesFor(List.of(item)).getOrDefault(id, List.of()));
    }

    public PublicContentResponse findByIdForCompatibility(UUID id) {
        ContentItem item = contentRepository.findPublishedByIdForCompatibility(id)
                .orElseThrow(() -> notFound());
        return toResponse(item, categoriesFor(List.of(item)).getOrDefault(id, List.of()));
    }

    public PublicContentResponse findBySlug(String type, String slug) {
        ContentType contentType = requireType(type);
        String normalizedSlug = normalizeSlug(slug);
        ContentItem item = contentRepository.findPublicBySlug(contentType, "ar", normalizedSlug)
                .orElseThrow(() -> notFound());
        return toResponse(item, categoriesFor(List.of(item)).getOrDefault(item.getId(), List.of()));
    }

    private Map<UUID, List<PublicContentResponse.CategoryReference>> categoriesFor(
            Collection<ContentItem> items) {
        if (items.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = items.stream().map(ContentItem::getId).toList();
        return taxonomyRepository.findActiveCategoriesForContent(ids).stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getContentItem().getId(),
                        Collectors.mapping(this::toCategory, Collectors.toList())));
    }

    private Specification<ContentItem> publishedSpecification(
            ContentType contentType, UUID entityId, String categorySlug,
            OffsetDateTime publishedFrom, OffsetDateTime publishedToExclusive,
            boolean requireEditorialVerification) {
        return (root, query, criteria) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteria.isNotNull(root.get("publishedRevision")));
            predicates.add(criteria.isNull(root.get("archivedAt")));
            if (requireEditorialVerification) {
                predicates.add(criteria.equal(root.get("editorialVerificationStatus"),
                        ye.gov.pmo.content.domain.EditorialVerificationStatus.VERIFIED));
                predicates.add(criteria.equal(
                        root.get("editorialVerifiedRevision"), root.get("publishedRevision")));
            }
            if (contentType != null) {
                predicates.add(criteria.equal(root.get("contentType"), contentType));
            }
            if (entityId != null) {
                predicates.add(criteria.equal(root.get("primaryEntity").get("id"), entityId));
            }
            if (publishedFrom != null) {
                predicates.add(criteria.greaterThanOrEqualTo(root.get("lastPublishedAt"), publishedFrom));
            }
            if (publishedToExclusive != null) {
                predicates.add(criteria.lessThan(root.get("lastPublishedAt"), publishedToExclusive));
            }
            if (categorySlug != null) {
                var category = query.subquery(Integer.class);
                var assignment = category.from(ContentTaxonomyAssignment.class);
                category.select(criteria.literal(1));
                category.where(
                        criteria.equal(assignment.get("contentItem").get("id"), root.get("id")),
                        criteria.equal(assignment.get("term").get("taxonomyCode"), "CONTENT_CATEGORY"),
                        criteria.equal(assignment.get("term").get("slug"), categorySlug),
                        criteria.isTrue(assignment.get("term").get("active")));
                predicates.add(criteria.exists(category));
            }
            return criteria.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private PublicContentResponse.CategoryReference toCategory(ContentTaxonomyAssignment assignment) {
        TaxonomyTerm term = assignment.getTerm();
        return new PublicContentResponse.CategoryReference(term.getId(), term.getSlug(), term.getLabelAr());
    }

    private PublicContentResponse toResponse(
            ContentItem item, List<PublicContentResponse.CategoryReference> categories) {
        ContentRevision revision = item.getPublishedRevision();
        if (revision == null) {
            throw new IllegalStateException("Published content must reference a published revision");
        }
        GovernmentEntity entity = item.getPrimaryEntity();
        return new PublicContentResponse(
                item.getId(),
                item.getContentType().name(),
                item.getSlug(),
                item.getLocale(),
                contentPath(item.getContentType(), item.getSlug()),
                revision.getTitle(),
                revision.getSummary(),
                revision.getBody(),
                revision.getByline(),
                item.getLastPublishedAt(),
                new PublicContentResponse.EntityReference(
                        entity.getId(), entity.getOfficialNameAr(), entityPath(entity)),
                List.copyOf(categories));
    }

    private String contentPath(ContentType type, String slug) {
        String segment = switch (type) {
            case NEWS -> "news";
            case ANNOUNCEMENT -> "announcements";
            case DECISION -> "decisions";
            case DOCUMENT -> "documents";
        };
        return "/" + segment + "/" + slug;
    }

    private String entityPath(GovernmentEntity entity) {
        EntityType type = entity.getEntityType();
        return "PRIME_MINISTERS_OFFICE".equals(type.getCode())
                ? "/" + type.getPublicPathSegment()
                : "/" + type.getPublicPathSegment() + "/" + entity.getSlug();
    }

    private ContentType parseType(String value) {
        return value == null || value.isBlank() ? null : requireType(value);
    }

    private ContentType requireType(String value) {
        try {
            return ContentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type");
        }
    }

    private String normalizeCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return normalizeSlug(value);
    }

    private String normalizeSlug(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slug");
        }
        return normalized;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Published content not found");
    }
}
