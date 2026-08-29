package ye.gov.pmo.organization.dto;

import java.util.List;

public record EntityDirectoryResponse(
        List<GovernmentEntitySummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
