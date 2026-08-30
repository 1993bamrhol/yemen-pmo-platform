package ye.gov.pmo.services.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.services.entity.GovernmentServiceDetailItem;

public interface GovernmentServiceDetailItemRepository
        extends JpaRepository<GovernmentServiceDetailItem, UUID> {

    List<GovernmentServiceDetailItem> findAllByService_IdOrderBySectionTypeAscDisplayOrderAsc(UUID serviceId);

    void deleteAllByService_Id(UUID serviceId);
}
