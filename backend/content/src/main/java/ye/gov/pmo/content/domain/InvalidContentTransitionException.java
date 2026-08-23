package ye.gov.pmo.content.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidContentTransitionException extends RuntimeException {

    public InvalidContentTransitionException(ContentStatus status, ContentAction action) {
        super("Action " + action + " is not allowed from content status " + status);
    }
}
