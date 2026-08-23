package ye.gov.pmo.content.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ContentSeparationOfDutiesException extends RuntimeException {

    public ContentSeparationOfDutiesException(ContentAction action) {
        super("The revision author cannot perform " + action + " without platform break-glass authority");
    }
}
