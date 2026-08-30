package ye.gov.pmo.services.dto;

import ye.gov.pmo.services.domain.ServiceDeliveryChannel;

public record ServiceChannelResponse(
        ServiceDeliveryChannel type,
        int order,
        String label,
        String actionUrl,
        String instructions) {
}
