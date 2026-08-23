package ye.gov.pmo.shared.audit;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(Long actorUserId, String action, String resourceType, String resourceId,
                       UUID governmentEntityId, AuditOutcome outcome, String correlationId, String metadata) {
        repository.save(new AuditEvent(
                actorUserId,
                action,
                resourceType,
                resourceId,
                governmentEntityId,
                outcome,
                correlationId,
                metadata));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIndependent(Long actorUserId, String action, String resourceType, String resourceId,
                                  UUID governmentEntityId, AuditOutcome outcome,
                                  String correlationId, String metadata) {
        repository.save(new AuditEvent(actorUserId, action, resourceType, resourceId,
                governmentEntityId, outcome, correlationId, metadata));
    }
}
