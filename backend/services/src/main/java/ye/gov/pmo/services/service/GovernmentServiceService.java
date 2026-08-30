package ye.gov.pmo.services.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.identity.security.EntityAuthorization;
import ye.gov.pmo.organization.entity.EntityStatus;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.organization.service.GovernmentEntityService;
import ye.gov.pmo.services.domain.ServiceDetailSection;
import ye.gov.pmo.services.domain.ServiceLifecycleStatus;
import ye.gov.pmo.services.domain.ServicePublicationAction;
import ye.gov.pmo.services.domain.ServiceSourceType;
import ye.gov.pmo.services.domain.ServiceVerificationStatus;
import ye.gov.pmo.services.dto.GovernmentServiceAdminResponse;
import ye.gov.pmo.services.dto.GovernmentServiceRequest;
import ye.gov.pmo.services.dto.ServiceChannelRequest;
import ye.gov.pmo.services.dto.ServiceChannelResponse;
import ye.gov.pmo.services.dto.ServiceDetailItemRequest;
import ye.gov.pmo.services.dto.ServiceDetailItemResponse;
import ye.gov.pmo.services.dto.ServiceDetailResponse;
import ye.gov.pmo.services.dto.ServiceDirectoryResponse;
import ye.gov.pmo.services.dto.ServiceOwnerResponse;
import ye.gov.pmo.services.dto.ServicePublicationRequest;
import ye.gov.pmo.services.dto.ServiceSourceResponse;
import ye.gov.pmo.services.dto.ServiceSummaryResponse;
import ye.gov.pmo.services.dto.ServiceVerificationRequest;
import ye.gov.pmo.services.entity.GovernmentService;
import ye.gov.pmo.services.entity.GovernmentServiceChannel;
import ye.gov.pmo.services.entity.GovernmentServiceDetailItem;
import ye.gov.pmo.services.repository.GovernmentServiceChannelRepository;
import ye.gov.pmo.services.repository.GovernmentServiceDetailItemRepository;
import ye.gov.pmo.services.repository.GovernmentServiceRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
@Transactional(readOnly = true)
public class GovernmentServiceService {

    private static final String PUBLIC_LOCALE = "ar";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SECTION_ITEMS = 100;
    private static final int MAX_CHANNELS = 10;

    private final GovernmentServiceRepository services;
    private final GovernmentServiceDetailItemRepository detailItems;
    private final GovernmentServiceChannelRepository channels;
    private final GovernmentEntityService entities;
    private final EntityAuthorization authorization;
    private final CurrentActorProvider actorProvider;
    private final AuditService auditService;

    public GovernmentServiceService(
            GovernmentServiceRepository services,
            GovernmentServiceDetailItemRepository detailItems,
            GovernmentServiceChannelRepository channels,
            GovernmentEntityService entities,
            EntityAuthorization authorization,
            CurrentActorProvider actorProvider,
            AuditService auditService) {
        this.services = services;
        this.detailItems = detailItems;
        this.channels = channels;
        this.entities = entities;
        this.authorization = authorization;
        this.actorProvider = actorProvider;
        this.auditService = auditService;
    }

    public ServiceDirectoryResponse findPublicServices(UUID entityId, int page, int size) {
        validatePage(page, size);
        if (entityId != null) entities.findPublicById(entityId);
        Specification<GovernmentService> specification = publicSpecification(entityId);
        Page<GovernmentService> result = services.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(
                        Sort.Order.asc("officialNameAr"), Sort.Order.asc("id"))));
        Map<UUID, List<GovernmentServiceChannel>> channelMap = channelsByService(result.getContent());
        return new ServiceDirectoryResponse(
                result.getContent().stream()
                        .map(service -> toSummary(service, channelMap.getOrDefault(service.getId(), List.of())))
                        .toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public ServiceDetailResponse findPublicById(UUID id) {
        return toDetail(findPublicService(services.findById(id)));
    }

    public ServiceDetailResponse findPublicBySlug(String slug) {
        return toDetail(findPublicService(services.findBySlug(normalizeSlug(slug))));
    }

    @Transactional
    public GovernmentServiceAdminResponse create(GovernmentServiceRequest request, String correlationId) {
        GovernmentEntity owner = entities.findEntity(request.owningEntityId());
        authorize(owner.getId(), "services.write", "services.manage");
        String slug = normalizeSlug(request.slug());
        if (services.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service slug already exists");
        }
        validateComponents(request);
        Long actorId = requiredActor();
        GovernmentService service = services.saveAndFlush(new GovernmentService(
                owner,
                slug,
                request.officialNameAr().trim(),
                trimToNull(request.officialNameEn()),
                trimToNull(request.summaryAr()),
                trimToNull(request.descriptionAr()),
                trimToNull(request.feesAr()),
                trimToNull(request.processingTimeAr()),
                actorId));
        replaceComponents(service, request);
        auditService.record(actorId, "SERVICE_CREATED", "GovernmentService", service.getId().toString(),
                owner.getId(), AuditOutcome.SUCCESS, correlationId, "slug=" + service.getSlug());
        return toAdmin(service);
    }

    @Transactional
    public GovernmentServiceAdminResponse update(
            UUID id, GovernmentServiceRequest request, String correlationId) {
        GovernmentService service = findService(id);
        authorize(service.getOwningEntity().getId(), "services.write", "services.manage");
        String requestedSlug = normalizeSlug(request.slug());
        if (!service.getSlug().equals(requestedSlug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service slug is immutable");
        }
        GovernmentEntity requestedOwner = entities.findEntity(request.owningEntityId());
        if (!requestedOwner.getId().equals(service.getOwningEntity().getId())) {
            authorize(requestedOwner.getId(), "services.write", "services.manage");
        }
        validateComponents(request);
        Long actorId = requiredActor();
        service.updateProfile(
                requestedOwner,
                request.officialNameAr().trim(),
                trimToNull(request.officialNameEn()),
                trimToNull(request.summaryAr()),
                trimToNull(request.descriptionAr()),
                trimToNull(request.feesAr()),
                trimToNull(request.processingTimeAr()),
                actorId);
        GovernmentService saved = services.saveAndFlush(service);
        replaceComponents(saved, request);
        auditService.record(actorId, "SERVICE_UPDATED", "GovernmentService", id.toString(),
                requestedOwner.getId(), AuditOutcome.SUCCESS, correlationId,
                "verificationReset=UNVERIFIED");
        return toAdmin(saved);
    }

    @Transactional
    public GovernmentServiceAdminResponse updatePublication(
            UUID id, ServicePublicationRequest request, String correlationId) {
        GovernmentService service = findService(id);
        authorize(service.getOwningEntity().getId(), "services.publish", "services.manage");
        Long actorId = requiredActor();
        OffsetDateTime now = OffsetDateTime.now();
        if (request.action() == ServicePublicationAction.PUBLISH) {
            service.publish(actorId, now);
        } else {
            service.archive(actorId, now);
        }
        GovernmentService saved = services.saveAndFlush(service);
        auditService.record(actorId, "SERVICE_" + request.action(), "GovernmentService", id.toString(),
                saved.getOwningEntity().getId(), AuditOutcome.SUCCESS, correlationId,
                "verificationStatus=" + saved.getVerificationStatus());
        return toAdmin(saved);
    }

    @Transactional
    public GovernmentServiceAdminResponse updateVerification(
            UUID id, ServiceVerificationRequest request, String correlationId) {
        GovernmentService service = findService(id);
        authorize(service.getOwningEntity().getId(), "services.publish", "services.manage");
        validateProvenance(request);
        Long actorId = requiredActor();
        try {
            service.updateVerification(
                    request.status(), request.sourceType(), trimToNull(request.sourceReference()),
                    actorId, OffsetDateTime.now());
            GovernmentService saved = services.saveAndFlush(service);
            auditService.record(actorId, verificationAuditAction(request.status()),
                    "GovernmentService", id.toString(), saved.getOwningEntity().getId(),
                    AuditOutcome.SUCCESS, correlationId,
                    "status=" + saved.getVerificationStatus()
                            + ";sourceType=" + saved.getProvenanceSourceType());
            return toAdmin(saved);
        } catch (IllegalStateException exception) {
            auditService.recordIndependent(actorId, verificationAuditAction(request.status()),
                    "GovernmentService", id.toString(), service.getOwningEntity().getId(),
                    AuditOutcome.FAILURE, correlationId,
                    "lifecycleStatus=" + service.getLifecycleStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    private Specification<GovernmentService> publicSpecification(UUID entityId) {
        return (root, query, criteria) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteria.equal(root.get("lifecycleStatus"), ServiceLifecycleStatus.PUBLISHED));
            predicates.add(criteria.equal(root.get("verificationStatus"), ServiceVerificationStatus.VERIFIED));
            predicates.add(criteria.equal(root.get("owningEntity").get("status"), EntityStatus.ACTIVE));
            predicates.add(criteria.isTrue(root.get("owningEntity").get("entityType").get("active")));
            if (entityId != null) {
                predicates.add(criteria.equal(root.get("owningEntity").get("id"), entityId));
            }
            return criteria.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private GovernmentService findPublicService(java.util.Optional<GovernmentService> candidate) {
        return candidate.filter(GovernmentService::isPubliclyEligible)
                .filter(service -> service.getOwningEntity().getStatus() == EntityStatus.ACTIVE)
                .filter(service -> service.getOwningEntity().getEntityType().isActive())
                .orElseThrow(this::notFound);
    }

    private GovernmentService findService(UUID id) {
        return services.findById(id).orElseThrow(this::notFound);
    }

    private void authorize(UUID entityId, String... permissions) {
        if (!authorization.hasPermission(entityId, permissions)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Service action is not allowed");
        }
    }

    private void replaceComponents(GovernmentService service, GovernmentServiceRequest request) {
        detailItems.deleteAllByService_Id(service.getId());
        detailItems.flush();
        channels.deleteAllByService_Id(service.getId());
        channels.flush();
        detailItems.saveAll(detailEntities(service, ServiceDetailSection.ELIGIBILITY, request.eligibility()));
        detailItems.saveAll(detailEntities(service, ServiceDetailSection.REQUIREMENT, request.requirements()));
        detailItems.saveAll(detailEntities(service, ServiceDetailSection.STEP, request.steps()));
        channels.saveAll(channelEntities(service, request.channels()));
    }

    private List<GovernmentServiceDetailItem> detailEntities(
            GovernmentService service, ServiceDetailSection section,
            List<ServiceDetailItemRequest> requests) {
        if (requests == null) return List.of();
        List<GovernmentServiceDetailItem> result = new ArrayList<>();
        for (int index = 0; index < requests.size(); index++) {
            ServiceDetailItemRequest request = requests.get(index);
            result.add(new GovernmentServiceDetailItem(
                    service, section, index + 1, request.title().trim(),
                    trimToNull(request.description())));
        }
        return result;
    }

    private List<GovernmentServiceChannel> channelEntities(
            GovernmentService service, List<ServiceChannelRequest> requests) {
        if (requests == null) return List.of();
        List<GovernmentServiceChannel> result = new ArrayList<>();
        for (int index = 0; index < requests.size(); index++) {
            ServiceChannelRequest request = requests.get(index);
            result.add(new GovernmentServiceChannel(
                    service, request.type(), index + 1, trimToNull(request.label()),
                    trimToNull(request.actionUrl()), trimToNull(request.instructions())));
        }
        return result;
    }

    private void validateComponents(GovernmentServiceRequest request) {
        validateSectionSize(request.eligibility(), "eligibility");
        validateSectionSize(request.requirements(), "requirements");
        validateSectionSize(request.steps(), "steps");
        List<ServiceChannelRequest> requestedChannels = request.channels() == null
                ? List.of() : request.channels();
        if (requestedChannels.size() > MAX_CHANNELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channels exceeds the maximum size");
        }
        requestedChannels.stream()
                .map(ServiceChannelRequest::actionUrl)
                .filter(value -> value != null && !value.isBlank())
                .forEach(this::validateHttpsUrl);
    }

    private void validateSectionSize(List<?> values, String field) {
        if (values != null && values.size() > MAX_SECTION_ITEMS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " exceeds the maximum size");
        }
    }

    private void validateProvenance(ServiceVerificationRequest request) {
        boolean hasSourceType = request.sourceType() != null;
        boolean hasReference = request.sourceReference() != null
                && !request.sourceReference().isBlank();
        if (request.status() == ServiceVerificationStatus.VERIFIED) {
            if (!hasSourceType || !hasReference) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Verified services require sourceType and sourceReference");
            }
            if (request.sourceType() == ServiceSourceType.OFFICIAL_SOURCE_REFERENCE) {
                validateHttpsUrl(request.sourceReference());
            } else if (!request.sourceReference().trim().matches("[A-Za-z0-9][A-Za-z0-9._:/-]{2,999}")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Administrative source references must be stable identifiers");
            }
            return;
        }
        if (hasSourceType || hasReference) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provenance is only accepted for VERIFIED services");
        }
    }

    private void validateHttpsUrl(String value) {
        try {
            URI uri = new URI(value.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only absolute HTTPS URLs are allowed");
            }
        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL", exception);
        }
    }

    private ServiceDetailResponse toDetail(GovernmentService service) {
        List<GovernmentServiceDetailItem> items =
                detailItems.findAllByService_IdOrderBySectionTypeAscDisplayOrderAsc(service.getId());
        List<GovernmentServiceChannel> serviceChannels =
                channels.findAllByService_IdOrderByDisplayOrderAsc(service.getId());
        return new ServiceDetailResponse(
                service.getId(), PUBLIC_LOCALE, service.getSlug(), canonicalPath(service),
                service.getOfficialNameAr(), service.getOfficialNameEn(), service.getSummaryAr(),
                service.getDescriptionAr(), owner(service.getOwningEntity()),
                detailResponses(items, item -> item.getSectionType() == ServiceDetailSection.ELIGIBILITY),
                detailResponses(items, item -> item.getSectionType() == ServiceDetailSection.REQUIREMENT),
                detailResponses(items, item -> item.getSectionType() == ServiceDetailSection.STEP),
                service.getFeesAr(), service.getProcessingTimeAr(),
                serviceChannels.stream().map(this::toChannelResponse).toList(),
                source(service), service.getLastPublishedAt(), service.getUpdatedAt());
    }

    private GovernmentServiceAdminResponse toAdmin(GovernmentService service) {
        return new GovernmentServiceAdminResponse(
                toDetail(service), service.getLifecycleStatus(),
                new GovernmentServiceAdminResponse.Verification(
                        service.getVerificationStatus(), service.getProvenanceSourceType(),
                        service.getProvenanceSourceReference(), service.getVerifiedAt(),
                        service.getVerifiedBy()),
                service.getFirstPublishedAt(), service.getArchivedAt(), service.getCreatedAt(),
                service.getVersion());
    }

    private ServiceSummaryResponse toSummary(
            GovernmentService service, List<GovernmentServiceChannel> serviceChannels) {
        return new ServiceSummaryResponse(
                service.getId(), PUBLIC_LOCALE, service.getSlug(), canonicalPath(service),
                service.getOfficialNameAr(), service.getOfficialNameEn(), service.getSummaryAr(),
                owner(service.getOwningEntity()),
                serviceChannels.stream().map(GovernmentServiceChannel::getChannelType).distinct().toList(),
                service.getUpdatedAt());
    }

    private Map<UUID, List<GovernmentServiceChannel>> channelsByService(
            List<GovernmentService> pageItems) {
        if (pageItems.isEmpty()) return Map.of();
        return channels.findAllByService_IdInOrderByService_IdAscDisplayOrderAsc(
                        pageItems.stream().map(GovernmentService::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(
                        GovernmentServiceChannel::getServiceId,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private List<ServiceDetailItemResponse> detailResponses(
            List<GovernmentServiceDetailItem> items, Predicate<GovernmentServiceDetailItem> filter) {
        return items.stream().filter(filter)
                .map(item -> new ServiceDetailItemResponse(
                        item.getDisplayOrder(), item.getTitleAr(), item.getDescriptionAr()))
                .toList();
    }

    private ServiceChannelResponse toChannelResponse(GovernmentServiceChannel channel) {
        return new ServiceChannelResponse(
                channel.getChannelType(), channel.getDisplayOrder(), channel.getLabelAr(),
                channel.getActionUrl(), channel.getInstructionsAr());
    }

    private ServiceSourceResponse source(GovernmentService service) {
        if (service.getVerificationStatus() != ServiceVerificationStatus.VERIFIED) return null;
        String publicReference = service.getProvenanceSourceType()
                == ServiceSourceType.OFFICIAL_SOURCE_REFERENCE
                ? service.getProvenanceSourceReference()
                : null;
        return new ServiceSourceResponse(
                service.getProvenanceSourceType(), publicReference,
                service.getVerifiedAt());
    }

    private ServiceOwnerResponse owner(GovernmentEntity owner) {
        return new ServiceOwnerResponse(
                owner.getId(), owner.getOfficialNameAr(), entities.canonicalPath(owner));
    }

    private String canonicalPath(GovernmentService service) {
        return "/services/" + service.getSlug();
    }

    private String normalizeSlug(String slug) {
        if (slug == null || !slug.trim().matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid service slug");
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private Long requiredActor() {
        Long actorId = actorProvider.currentUserId();
        if (actorId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }
        return actorId;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "page must be >= 0 and size must be between 1 and 100");
        }
    }

    private String verificationAuditAction(ServiceVerificationStatus status) {
        return switch (status) {
            case VERIFIED -> "SERVICE_VERIFIED";
            case REJECTED -> "SERVICE_VERIFICATION_REJECTED";
            case UNVERIFIED -> "SERVICE_VERIFICATION_RESET";
        };
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Government service not found");
    }
}
