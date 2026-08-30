package ye.gov.pmo.services.dto;

import java.util.List;

public record ServiceDirectoryResponse(
        List<ServiceSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
