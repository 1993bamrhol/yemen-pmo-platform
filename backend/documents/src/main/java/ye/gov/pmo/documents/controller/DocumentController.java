package ye.gov.pmo.documents.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.documents.dto.DocumentResponse;
import ye.gov.pmo.documents.service.DocumentQuery;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentQuery documentService;

    public DocumentController(DocumentQuery documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentResponse> findAll() {
        return documentService.findAll();
    }

    @GetMapping("/{id}")
    public DocumentResponse findById(@PathVariable("id") Long id) {
        return documentService.findById(id);
    }
}
