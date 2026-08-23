package ye.gov.pmo.organization.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.organization.dto.EntityTypeResponse;
import ye.gov.pmo.organization.dto.GovernmentEntityRequest;
import ye.gov.pmo.organization.dto.GovernmentEntityResponse;
import ye.gov.pmo.organization.entity.EntityStatus;
import ye.gov.pmo.organization.entity.EntityType;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.organization.repository.EntityTypeRepository;
import ye.gov.pmo.organization.repository.GovernmentEntityRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
@Transactional(readOnly = true)
public class GovernmentEntityService {

    private final GovernmentEntityRepository entityRepository;
    private final EntityTypeRepository typeRepository;
    private final AuditService auditService;
    private final CurrentActorProvider actorProvider;

    public GovernmentEntityService(GovernmentEntityRepository entityRepository,
                                   EntityTypeRepository typeRepository,
                                   AuditService auditService,
                                   CurrentActorProvider actorProvider) {
        this.entityRepository = entityRepository;
        this.typeRepository = typeRepository;
        this.auditService = auditService;
        this.actorProvider = actorProvider;
    }

    public List<GovernmentEntityResponse> findPublicEntities() {
        return entityRepository.findAllByStatusOrderByOfficialNameArAsc(EntityStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    public GovernmentEntityResponse findPublicById(UUID id) {
        GovernmentEntity entity = findEntity(id);
        if (entity.getStatus() != EntityStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found");
        }
        return toResponse(entity);
    }

    public GovernmentEntityResponse findPublicBySlug(String pathSegment, String slug) {
        EntityType type = typeRepository.findByPublicPathSegment(pathSegment)
                .filter(EntityType::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity type not found"));
        GovernmentEntity entity = entityRepository.findByEntityTypeIdAndSlug(type.getId(), slug)
                .filter(item -> item.getStatus() == EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found"));
        return toResponse(entity);
    }

    public List<GovernmentEntityResponse> findPublicChildren(UUID parentId) {
        findPublicById(parentId);
        return entityRepository.findAllByParentIdAndStatusOrderByOfficialNameArAsc(parentId, EntityStatus.ACTIVE).stream()
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
        EntityType type = findActiveType(request.entityTypeCode());
        String slug = normalizeSlug(request.slug());
        if (entityRepository.existsByEntityTypeIdAndSlug(type.getId(), slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists for this entity type");
        }
        GovernmentEntity parent = resolveParent(request.parentEntityId(), null);
        Long actorId = actorProvider.currentUserId();
        GovernmentEntity saved = entityRepository.save(new GovernmentEntity(
                type,
                parent,
                request.officialNameAr().trim(),
                trimToNull(request.shortNameAr()),
                slug,
                parseStatus(request.status()),
                trimToNull(request.description()),
                trimToNull(request.websiteUrl()),
                actorId));
        auditService.record(actorId, "ENTITY_CREATED", "GovernmentEntity", saved.getId().toString(),
                saved.getId(), AuditOutcome.SUCCESS, null, "actor=" + actorProvider.currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public GovernmentEntityResponse update(UUID id, GovernmentEntityRequest request) {
        GovernmentEntity existing = findEntity(id);
        EntityType type = findActiveType(request.entityTypeCode());
        String slug = normalizeSlug(request.slug());
        if (entityRepository.existsByEntityTypeIdAndSlugAndIdNot(type.getId(), slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists for this entity type");
        }
        GovernmentEntity parent = resolveParent(request.parentEntityId(), id);
        Long actorId = actorProvider.currentUserId();
        existing.update(
                type,
                parent,
                request.officialNameAr().trim(),
                trimToNull(request.shortNameAr()),
                slug,
                parseStatus(request.status()),
                trimToNull(request.description()),
                trimToNull(request.websiteUrl()),
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
                ? null
                : new GovernmentEntityResponse.ParentReference(parent.getId(), parent.getOfficialNameAr(), canonicalPath(parent));
        return new GovernmentEntityResponse(
                entity.getId(),
                toTypeResponse(entity.getEntityType()),
                entity.getOfficialNameAr(),
                entity.getShortNameAr(),
                entity.getSlug(),
                canonicalPath,
                entity.getStatus().name(),
                entity.getDescription(),
                entity.getWebsiteUrl(),
                parentResponse);
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

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
