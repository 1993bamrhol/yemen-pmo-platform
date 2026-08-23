package ye.gov.pmo.bootstrap.backfill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BackfillApplyRequest(
        @NotNull Integer manifestSchemaVersion,
        @NotBlank String confirmation) {
}
