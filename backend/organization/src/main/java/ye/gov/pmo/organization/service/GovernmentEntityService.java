package ye.gov.pmo.organization.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.organization.dto.EntityTypeResponse;
import ye.gov.pmo.organization.dto.EntityDirectoryResponse;
import ye.gov.pmo.organization.dto.GovernmentEntityRequest;
import ye.gov.pmo.organization.dto.GovernmentEntityResponse;
import ye.gov.pmo.organization.dto.GovernmentEntitySummaryResponse;
import ye.gov.pmo.organization.entity.EntityStatus;
import ye.gov.pmo.organization.entity.EntityType;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.organization.entity.GovernmentEntitySlugAlias;
import ye.gov.pmo.organization.repository.EntityTypeRepository;
import ye.gov.pmo.organization.repository.GovernmentEntityRepository;
import ye.gov.pmo.organization.repository.GovernmentEntitySlugAliasRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
@Transactional(readOnly = true)
public class GovernmentEntityService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String PUBLIC_LOCALE = "ar";

    private final GovernmentEntityRepository entityRepository;
    private final EntityTypeRepository typeRepository;
    private final GovernmentEntitySlugAliasRepository aliasRepository;
    private final AuditService auditService;
    private final CurrentActorProvider actorProvider;

    public GovernmentEntityService(GovernmentEntityRepository entityRepository,
                                   EntityTypeRepository typeRepository,
                                   GovernmentEntitySlugAliasRepository aliasRepository,
                                   AuditService auditService,
                                   CurrentActorProvider actorProvider) {
        this.entityRepository = entityRepository;
        this.typeRepository = typeRepository;
        this.aliasRepository = aliasRepository;
        this.auditService = auditService;
        this.actorProvider = actorProvider;
    }

    public List<GovernmentEntityResponse> findPublicEntities() {
        return entityRepository.findAllByStatusOrderByOfficialNameArAsc(EntityStatus.ACTIVE).stream()
                .filter(entity -> entity.getEntityType().isActive())
                .map(this::toResponse)
                .toList();
    }

    public EntityDirectoryResponse findPublicDirectory(
            String typeCode, UUID parentId, int page, int size) {
        validatePage(page, size);
        EntityType requestedType = typeCode == null || typeCode.isBlank()
                ? null
                : findActiveType(typeCode);
        if (parentId != null) findPublicById(parentId);
        Specification<GovernmentEntity> specification = (root, query, criteria) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteria.equal(root.get("status"), EntityStatus.ACTIVE));
            predicates.add(criteria.isTrue(root.get("entityType").get("active")));
            if (requestedType != null) {
                predicates.add(criteria.equal(root.get("entityType").get("id"), requestedType.getId()));
            }
            if (parentId != null) {
                predicates.add(criteria.equal(root.get("parent").get("id"), parentId));
            }
            return criteria.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<GovernmentEntity> result = entityRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(
                        Sort.Order.asc("officialNameAr"), Sort.Order.asc("id"))));
        return new EntityDirectoryResponse(
                result.getContent().stream().map(this::toSummaryResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public GovernmentEntityResponse findPublicById(UUID id) {
        GovernmentEntity entity = findEntity(id);
        if (entity.getStatus() != EntityStatus.ACTIVE || !entity.getEntityType().isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found");
        }
        return toResponse(entity);
    }

    public GovernmentEntityResponse findPublicBySlug(String pathSegment, String slug) {
        String normalizedSegment = normalizeLocatorPart(pathSegment);
        String normalizedSlug = normalizeSlug(slug);
        Optional<GovernmentEntity> current = typeRepository.findByPublicPathSegment(normalizedSegment)
                .filter(EntityType::isActive)
                .flatMap(type -> entityRepository.findByEntityTypeIdAndSlug(type.getId(), normalizedSlug));
        GovernmentEntity entity = current
                .or(() -> aliasRepository.findByPublicPathSegmentAndSlug(normalizedSegment, normalizedSlug)
                        .map(GovernmentEntitySlugAlias::getGovernmentEntity))
                .filter(item -> item.getStatus() == EntityStatus.ACTIVE)
                .filter(item -> item.getEntityType().isActive())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found"));
        return toResponse(entity);
    }

    public List<GovernmentEntityResponse> findPublicChildren(UUID parentId) {
        findPublicById(parentId);
        return entityRepository.findAllByParentIdAndStatusOrderByOfficialNameArAsc(parentId, EntityStatus.ACTIVE).stream()
                .filter(entity -> entity.getEntityType().isActive())
                .map(this::toResponse)
                .toList();
    }

    public List<EntityTypeResponse> findActiveTypes() {
        return typeRepository.findAll().stream()
                .filter(EntityType::isActive)
                .map(this::toTypeResponse)
                .toList();
    }

    @Transactional
    public GovernmentEntityResponse create(GovernmentEntityRequest request) {
        EntityType type = findActiveTypeForMutation(request.entityTypeCode(), null);
        String slug = normalizeSlug(request.slug());
        ensureLocatorAvailable(type, slug, null);
        GovernmentEntity parent = resolveParent(request.parentEntityId(), null);
        Long actorId = actorProvider.currentUserId();
        GovernmentEntity saved = entityRepository.save(new GovernmentEntity(
                type,
                parent,
                request.officialNameAr().trim(),
                trimToNull(request.officialNameEn()),
                trimToNull(request.shortNameAr()),
                slug,
                parseStatus(request.status()),
                trimToNull(request.description()),
                trimToNull(request.mandate()),
                trimToNull(request.websiteUrl()),
                trimToNull(request.officialEmail()),
                trimToNull(request.officialPhone()),
                trimToNull(request.officialAddressAr()),
                trimToNull(request.officialSourceReference()),
                actorId));
        auditService.record(actorId, "ENTITY_CREATED", "GovernmentEntity", saved.getId().toString(),
                saved.getId(), AuditOutcome.SUCCESS, null, "actor=" + actorProvider.currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public GovernmentEntityResponse update(UUID id, GovernmentEntityRequest request) {
        GovernmentEntity existing = findEntity(id);
        EntityType type = findActiveTypeForMutation(
                request.entityTypeCode(), existing.getEntityType().getId());
        String slug = normalizeSlug(request.slug());
        String oldPathSegment = existing.getEntityType().getPublicPathSegment();
        String oldSlug = existing.getSlug();
        ensureLocatorAvailable(type, slug, id);
        GovernmentEntity parent = resolveParent(request.parentEntityId(), id);
        Long actorId = actorProvider.currentUserId();
        reclaimAliasForCurrentLocator(type.getPublicPathSegment(), slug, id);
        preserveOldLocator(existing, oldPathSegment, oldSlug, type.getPublicPathSegment(), slug, actorId);
        existing.update(
                type,
                parent,
                request.officialNameAr().trim(),
                trimToNull(request.officialNameEn()),
                trimToNull(request.shortNameAr()),
                slug,
                parseStatus(request.status()),
                trimToNull(request.description()),
                trimToNull(request.mandate()),
                trimToNull(request.websiteUrl()),
                trimToNull(request.officialEmail()),
                trimToNull(request.officialPhone()),
                trimToNull(request.officialAddressAr()),
                trimToNull(request.officialSourceReference()),
                actorId);
        GovernmentEntity saved = entityRepository.save(existing);
        auditService.record(actorId, "ENTITY_UPDATED", "GovernmentEntity", saved.getId().toString(),
                saved.getId(), AuditOutcome.SUCCESS, null, "actor=" + actorProvider.currentUsername());
        return toResponse(saved);
    }

    public GovernmentEntity findEntity(UUID id) {
        return entityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found"));
    }

    private EntityType findActiveType(String code) {
        return typeRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .filter(EntityType::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported entity type"));
    }

    private EntityType findActiveTypeForMutation(String code, Long existingTypeId) {
        EntityType requested = findActiveType(code);
        LinkedHashSet<Long> typeIds = new LinkedHashSet<>();
        if (existingTypeId != null) typeIds.add(existingTypeId);
        typeIds.add(requested.getId());
        return typeRepository.lockAllByIdInOrder(typeIds).stream()
                .filter(type -> type.getId().equals(requested.getId()) && type.isActive())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unsupported entity type"));
    }

    private GovernmentEntity resolveParent(UUID parentId, UUID currentEntityId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(currentEntityId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An entity cannot be its own parent");
        }
        GovernmentEntity parent = findEntity(parentId);
        GovernmentEntity cursor = parent;
        while (cursor != null) {
            if (cursor.getId().equals(currentEntityId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entity hierarchy cannot contain a cycle");
            }
            cursor = cursor.getParent();
        }
        return parent;
    }

    private GovernmentEntityResponse toResponse(GovernmentEntity entity) {
        String canonicalPath = canonicalPath(entity);
        GovernmentEntity parent = entity.getParent();
        GovernmentEntityResponse.ParentReference parentResponse = parent == null
                || parent.getStatus() != EntityStatus.ACTIVE
                || !parent.getEntityType().isActive()
                ? null
                : new GovernmentEntityResponse.ParentReference(parent.getId(), parent.getOfficialNameAr(), canonicalPath(parent));
        return new GovernmentEntityResponse(
                entity.getId(),
                PUBLIC_LOCALE,
                toTypeResponse(entity.getEntityType()),
                entity.getOfficialNameAr(),
                entity.getOfficialNameEn(),
                entity.getShortNameAr(),
                entity.getSlug(),
                canonicalPath,
                entity.getStatus().name(),
                entity.getDescription(),
                entity.getMandate(),
                entity.getWebsiteUrl(),
                contact(entity),
                entity.getOfficialSourceReference(),
                entity.getUpdatedAt(),
                parentResponse);
    }

    private GovernmentEntitySummaryResponse toSummaryResponse(GovernmentEntity entity) {
        return new GovernmentEntitySummaryResponse(
                entity.getId(), PUBLIC_LOCALE, toTypeResponse(entity.getEntityType()),
                entity.getOfficialNameAr(), entity.getShortNameAr(), entity.getSlug(),
                canonicalPath(entity), entity.getDescription(), entity.getUpdatedAt());
    }

    private GovernmentEntityResponse.Contact contact(GovernmentEntity entity) {
        if (entity.getOfficialEmail() == null
                && entity.getOfficialPhone() == null
                && entity.getOfficialAddressAr() == null) {
            return null;
        }
        return new GovernmentEntityResponse.Contact(
                entity.getOfficialEmail(), entity.getOfficialPhone(), entity.getOfficialAddressAr());
    }

    private EntityTypeResponse toTypeResponse(EntityType type) {
        return new EntityTypeResponse(type.getId(), type.getCode(), type.getNameAr(), type.getPublicPathSegment());
    }

    private String canonicalPath(GovernmentEntity entity) {
        String segment = entity.getEntityType().getPublicPathSegment();
        return "PRIME_MINISTERS_OFFICE".equals(entity.getEntityType().getCode())
                ? "/" + segment
                : "/" + segment + "/" + entity.getSlug();
    }

    private EntityStatus parseStatus(String status) {
        try {
            return EntityStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported entity status");
        }
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLocatorPart(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureLocatorAvailable(EntityType type, String slug, UUID currentEntityId) {
        boolean currentConflict = currentEntityId == null
                ? entityRepository.existsByEntityTypeIdAndSlug(type.getId(), slug)
                : entityRepository.existsByEntityTypeIdAndSlugAndIdNot(type.getId(), slug, currentEntityId);
        if (currentConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists for this entity type");
        }
        aliasRepository.findByPublicPathSegmentAndSlug(type.getPublicPathSegment(), slug)
                .filter(alias -> currentEntityId == null
                        || !alias.getGovernmentEntity().getId().equals(currentEntityId))
                .ifPresent(alias -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug is reserved by an existing alias");
                });
    }

    private void reclaimAliasForCurrentLocator(String pathSegment, String slug, UUID entityId) {
        aliasRepository.findByPublicPathSegmentAndSlug(pathSegment, slug)
                .filter(alias -> alias.getGovernmentEntity().getId().equals(entityId))
                .ifPresent(aliasRepository::delete);
    }

    private void preserveOldLocator(
            GovernmentEntity entity, String oldPathSegment, String oldSlug,
            String newPathSegment, String newSlug, Long actorId) {
        if (oldPathSegment.equals(newPathSegment) && oldSlug.equals(newSlug)) return;
        if (aliasRepository.findByPublicPathSegmentAndSlug(oldPathSegment, oldSlug).isEmpty()) {
            aliasRepository.save(new GovernmentEntitySlugAlias(
                    entity, oldPathSegment, oldSlug, actorId));
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "page must be >= 0 and size must be between 1 and 100");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
