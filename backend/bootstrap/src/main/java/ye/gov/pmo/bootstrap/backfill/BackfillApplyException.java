package ye.gov.pmo.bootstrap.backfill;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BackfillApplyException extends RuntimeException {
    public BackfillApplyException(String message) {
        super(message);
    }
}
