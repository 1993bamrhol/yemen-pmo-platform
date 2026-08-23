package ye.gov.pmo.organization.service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.organization.dto.EntityRelationshipRequest;
import ye.gov.pmo.organization.dto.EntityRelationshipResponse;
import ye.gov.pmo.organization.entity.EntityRelationship;
import ye.gov.pmo.organization.entity.EntityRelationshipType;
import ye.gov.pmo.organization.entity.GovernmentEntity;
import ye.gov.pmo.organization.repository.EntityRelationshipRepository;
import ye.gov.pmo.shared.audit.AuditOutcome;
import ye.gov.pmo.shared.audit.AuditService;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Service
public class EntityRelationshipService {

    private final EntityRelationshipRepository repository;
    private final GovernmentEntityService entityService;
    private final CurrentActorProvider actorProvider;
    private final AuditService auditService;

    public EntityRelationshipService(EntityRelationshipRepository repository,
                                     GovernmentEntityService entityService,
                                     CurrentActorProvider actorProvider,
                                     AuditService auditService) {
        this.repository = repository;
        this.entityService = entityService;
        this.actorProvider = actorProvider;
        this.auditService = auditService;
    }

    @Transactional
    public EntityRelationshipResponse create(UUID sourceEntityId, EntityRelationshipRequest request) {
        if (sourceEntityId.equals(request.targetEntityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An entity cannot relate to itself");
        }
        validateDates(request.validFrom(), request.validTo());
        EntityRelationshipType type = parseType(request.relationshipType());
        GovernmentEntity source = entityService.findEntity(sourceEntityId);
        GovernmentEntity target = entityService.findEntity(request.targetEntityId());
        if (repository.existsBySourceIdAndTargetIdAndRelationshipType(sourceEntityId, target.getId(), type)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entity relationship already exists");
        }
        Long actorId = actorProvider.currentUserId();
        EntityRelationship saved = repository.save(
                new EntityRelationship(source, target, type, request.validFrom(), request.validTo(), actorId));
        auditService.record(actorId, "ENTITY_RELATIONSHIP_CREATED", "EntityRelationship",
                saved.getId().toString(), sourceEntityId, AuditOutcome.SUCCESS, null,
                "type=" + type + ";target=" + target.getId());
        return new EntityRelationshipResponse(saved.getId(), sourceEntityId, target.getId(), type.name(),
                saved.getValidFrom(), saved.getValidTo(), saved.getCreatedAt());
    }

    private EntityRelationshipType parseType(String type) {
        try {
            return EntityRelationshipType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported relationship type");
        }
    }

    private void validateDates(LocalDate validFrom, LocalDate validTo) {
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validTo must not be before validFrom");
        }
    }
}
