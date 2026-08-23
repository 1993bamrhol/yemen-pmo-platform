package ye.gov.pmo.content.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.content.domain.ContentAction;
import ye.gov.pmo.content.domain.ContentSeparationOfDutiesException;
import ye.gov.pmo.content.domain.ContentStatus;
import ye.gov.pmo.content.domain.ContentType;
import ye.gov.pmo.content.domain.ContentWorkflowPolicy;
import ye.gov.pmo.content.domain.InvalidContentTransitionException;
import ye.gov.pmo.content.dto.AdminContentResponse;
import ye.gov.pmo.content.dto.ContentCreateRequest;
import ye.gov.pmo.content.dto.ContentRevisionRequest;
import ye.gov.pmo.content.dto.ContentTransitionRequest;
import ye.gov.pmo.content.dto.ContentTransitionResponse;
import ye.gov.pmo.content.dto.PageResponse;
import ye.gov.pmo.content.entity.ContentItem;
import ye.gov.pmo.content.entity.ContentRevision;
import ye.gov.pmo.content.entity.ContentTaxonomyAssignment;
import ye.gov.pmo.content.entity.ContentTransition;
import ye.gov.pmo.content.repository.ContentItemRepository;
import ye.gov.pmo.content.repository.ContentRevisionRepository;
import ye.gov.pmo.content.repository.ContentTaxonomyAssignmentRepository;
import ye.gov.pmo.content.repository.ContentTransitionRepository;
import ye.gov.pmo.content.repository.TaxonomyTermRepository;
import ye.gov.pmo.identity.security.EntityAuthorization;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.organization.repository.GovernmentEntityRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
@Transactional(readOnly = true)
public class AdminContentService {
    private static final int MAX_PAGE_SIZE = 100;

    private final ContentItemRepository items;
    private final ContentRevisionRepository revisions;
    private final ContentTransitionRepository transitions;
    private final ContentTaxonomyAssignmentRepository assignments;
    private final TaxonomyTermRepository terms;
    private final GovernmentEntityRepository entities;
    private final EntityAuthorization authorization;
    private final CurrentActorProvider actorProvider;
    private final AuditService auditService;
    private final ContentHtmlSanitizer sanitizer;
    private final ContentWorkflowPolicy workflow = new ContentWorkflowPolicy();

    public AdminContentService(ContentItemRepository items, ContentRevisionRepository revisions,
                               ContentTransitionRepository transitions,
                               ContentTaxonomyAssignmentRepository assignments,
                               TaxonomyTermRepository terms, GovernmentEntityRepository entities,
                               EntityAuthorization authorization, CurrentActorProvider actorProvider,
                               AuditService auditService, ContentHtmlSanitizer sanitizer) {
        this.items = items;
        this.revisions = revisions;
        this.transitions = transitions;
        this.assignments = assignments;
        this.terms = terms;
        this.entities = entities;
        this.authorization = authorization;
        this.actorProvider = actorProvider;
        this.auditService = auditService;
        this.sanitizer = sanitizer;
    }

    public PageResponse<AdminContentResponse> findForEntity(UUID entityId, ContentStatus status,
                                                             ContentType type, int page, int size) {
        validatePage(page, size);
        Page<ContentItem> result = items.findForAdministration(entityId, status, type, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public AdminContentResponse findById(UUID id) {
        return toResponse(loadAuthorized(id, "content.read", "content.manage"));
    }

    @Transactional
    public AdminContentResponse create(UUID entityId, ContentCreateRequest request) {
        Long actorId = requiredActor();
        GovernmentEntity entity = entities.findById(entityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Government entity not found"));
        if (items.existsByContentTypeAndLocaleAndSlug(request.contentType(), request.locale(), request.slug())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content slug already exists for type and locale");
        }
        ContentItem item = items.save(new ContentItem(
                request.contentType(), entity, request.slug(), request.locale(), actorId));
        ContentRevision revision = revisions.save(new ContentRevision(item, 1, request.title(), request.summary(),
                sanitizeBody(request.body()), request.byline(), "Initial revision", actorId));
        item.setCurrentRevision(revision, actorId);
        applyCategories(item, request.categorySlugs(), actorId);
        ContentItem saved = items.save(item);
        auditService.record(actorId, "CONTENT_CREATED", "ContentItem", saved.getId().toString(),
                entityId, AuditOutcome.SUCCESS, null, "type=" + saved.getContentType());
        return toResponse(saved);
    }

    @Transactional
    public AdminContentResponse createRevision(UUID id, ContentRevisionRequest request) {
        ContentItem item = loadAuthorized(id, "content.write", "content.manage");
        if (item.getStatus() != ContentStatus.DRAFT && item.getStatus() != ContentStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Revisions can only be added while content is in DRAFT or PUBLISHED");
        }
        Long actorId = requiredActor();
        int number = revisions.findMaxRevisionNumber(id) + 1;
        ContentRevision revision = revisions.save(new ContentRevision(item, number, request.title(),
                request.summary(), sanitizeBody(request.body()), request.byline(), request.changeNote(), actorId));
        item.setCurrentRevision(revision, actorId);
        ContentItem saved = items.save(item);
        auditService.record(actorId, "CONTENT_REVISION_CREATED", "ContentItem", id.toString(),
                item.getPrimaryEntity().getId(), AuditOutcome.SUCCESS, null, "revision=" + number);
        return toResponse(saved);
    }

    @Transactional
    public ContentTransitionResponse transition(UUID id, ContentTransitionRequest request, String correlationId) {
        ContentItem item = items.findForAdministrationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content item not found"));
        UUID entityId = item.getPrimaryEntity().getId();
        authorizeAction(entityId, request.action());
        Long actorId = requiredActor();
        boolean breakGlass = validateBreakGlass(request, actorId);
        ContentStatus from = item.getStatus();
        try {
            ContentStatus to = workflow.transition(from, request.action());
            ContentRevision revision = item.getCurrentRevision();
            if (revision == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Content has no current revision");
            }
            workflow.enforceSeparationOfDuties(request.action(), actorId, revision.getCreatedBy(), breakGlass);
            OffsetDateTime now = OffsetDateTime.now();
            item.transitionTo(to, actorId, now);
            items.save(item);
            ContentTransition saved = transitions.save(new ContentTransition(item, revision, from, to,
                    request.action(), actorId, entityId, request.comment(), correlationId, now));
            auditService.record(actorId, "CONTENT_" + request.action(), "ContentItem", id.toString(),
                    entityId, AuditOutcome.SUCCESS, correlationId,
                    "from=" + from + ";to=" + to + ";breakGlass=" + breakGlass);
            return new ContentTransitionResponse(saved.getId(), from, to, request.action(), actorId,
                    request.comment(), now);
        } catch (InvalidContentTransitionException | ContentSeparationOfDutiesException exception) {
            auditService.recordIndependent(actorId, "CONTENT_" + request.action(), "ContentItem", id.toString(),
                    entityId, AuditOutcome.FAILURE, correlationId, exception.getMessage());
            throw exception;
        }
    }

    private ContentItem loadAuthorized(UUID id, String... permissions) {
        ContentItem item = items.findForAdministrationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content item not found"));
        if (!authorization.hasPermission(item.getPrimaryEntity().getId(), permissions)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for content entity");
        }
        return item;
    }

    private void authorizeAction(UUID entityId, ContentAction action) {
        boolean allowed = switch (action) {
            case SUBMIT_REVIEW -> authorization.hasPermission(entityId, "content.write", "content.manage");
            case REQUEST_CHANGES -> authorization.hasPermission(entityId, "content.review", "content.manage");
            case APPROVE -> authorization.hasPermission(entityId, "content.approve", "content.manage");
            case PUBLISH -> authorization.hasPermission(entityId, "content.publish", "content.manage");
            case ARCHIVE -> authorization.hasPermission(entityId, "content.archive", "content.manage");
            case RESTORE -> authorization.hasPlatformPermission("content.manage");
        };
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Content action is not allowed");
    }

    private boolean validateBreakGlass(ContentTransitionRequest request, Long actorId) {
        if (!request.breakGlass()) return false;
        if (request.action() != ContentAction.APPROVE && request.action() != ContentAction.PUBLISH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Break-glass is only valid for approve or publish");
        }
        if (request.comment() == null || request.comment().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Break-glass requires an audit comment");
        }
        if (!authorization.hasPlatformPermission("content.manage")) {
            auditService.recordIndependent(actorId, "CONTENT_BREAK_GLASS_DENIED", "ContentItem", null,
                    null, AuditOutcome.DENIED, null, "action=" + request.action());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Platform break-glass authority is required");
        }
        return true;
    }

    private void applyCategories(ContentItem item, Set<String> requestedSlugs, Long actorId) {
        if (requestedSlugs == null || requestedSlugs.isEmpty()) return;
        Set<String> slugs = new LinkedHashSet<>(requestedSlugs);
        var found = terms.findAllByTaxonomyCodeAndSlugInAndActiveTrue("CONTENT_CATEGORY", slugs);
        if (found.size() != slugs.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more content categories are invalid");
        }
        assignments.saveAll(found.stream()
                .map(term -> new ContentTaxonomyAssignment(item, term, actorId)).toList());
    }

    private String sanitizeBody(String body) {
        String sanitized = sanitizer.sanitize(body);
        if (sanitized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content body is empty after sanitization");
        }
        return sanitized;
    }

    private Long requiredActor() {
        Long actorId = actorProvider.currentUserId();
        if (actorId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        return actorId;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0 and size must be between 1 and 100");
        }
    }

    private AdminContentResponse toResponse(ContentItem item) {
        return new AdminContentResponse(item.getId(), item.getContentType(), item.getPrimaryEntity().getId(),
                item.getSlug(), item.getLocale(), item.getStatus(), revision(item.getCurrentRevision()),
                revision(item.getPublishedRevision()), item.getFirstPublishedAt(), item.getLastPublishedAt(),
                item.getArchivedAt(), item.getCreatedAt(), item.getUpdatedAt(), item.getVersion());
    }

    private AdminContentResponse.Revision revision(ContentRevision revision) {
        if (revision == null) return null;
        return new AdminContentResponse.Revision(revision.getId(), revision.getRevisionNumber(),
                revision.getTitle(), revision.getSummary(), revision.getBody(), revision.getByline(),
                revision.getChangeNote(), revision.getCreatedAt(), revision.getCreatedBy());
    }
}
