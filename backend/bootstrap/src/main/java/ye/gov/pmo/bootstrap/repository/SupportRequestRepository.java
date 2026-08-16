package ye.gov.pmo.bootstrap.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.bootstrap.entity.SupportRequest;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findAllByOrderByCreatedAtDesc();
}
