package ye.gov.pmo.documents.service;

import java.util.List;
import ye.gov.pmo.documents.dto.DocumentResponse;

public interface DocumentQuery {
    List<DocumentResponse> findAll();

    DocumentResponse findById(Long id);
}
