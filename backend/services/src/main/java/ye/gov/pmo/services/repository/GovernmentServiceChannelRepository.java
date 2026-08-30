package ye.gov.pmo.services.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.services.entity.GovernmentServiceChannel;

public interface GovernmentServiceChannelRepository
        extends JpaRepository<GovernmentServiceChannel, UUID> {

    List<GovernmentServiceChannel> findAllByService_IdOrderByDisplayOrderAsc(UUID serviceId);

    List<GovernmentServiceChannel> findAllByService_IdInOrderByService_IdAscDisplayOrderAsc(
            Collection<UUID> serviceIds);

    void deleteAllByService_Id(UUID serviceId);
}
