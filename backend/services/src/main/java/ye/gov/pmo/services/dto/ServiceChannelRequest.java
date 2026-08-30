package ye.gov.pmo.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ye.gov.pmo.services.domain.ServiceDeliveryChannel;

public record ServiceChannelRequest(
        @NotNull ServiceDeliveryChannel type,
        @Size(max = 255) String label,
        @Size(max = 1000) String actionUrl,
        @Size(max = 2000) String instructions) {
}
