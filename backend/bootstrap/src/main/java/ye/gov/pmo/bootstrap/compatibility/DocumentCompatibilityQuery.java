package ye.gov.pmo.bootstrap.compatibility;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ye.gov.pmo.documents.dto.DocumentResponse;
import ye.gov.pmo.documents.service.DocumentQuery;
import ye.gov.pmo.documents.service.DocumentService;

@Service
@Primary
public class DocumentCompatibilityQuery implements DocumentQuery {
    private final DocumentService legacy;
    private final UnifiedLegacyProjectionService unified;
    private final ContentCompatibilityRouter router;

    public DocumentCompatibilityQuery(DocumentService legacy, UnifiedLegacyProjectionService unified,
                                      ContentCompatibilityRouter router) {
        this.legacy = legacy;
        this.unified = unified;
        this.router = router;
    }

    @Override
    public List<DocumentResponse> findAll() {
        if (!router.useUnified("DOCUMENT")) return legacy.findAll();
        try {
            return unified.findAll("DOCUMENT", "STATIC_DOCUMENTS").stream().map(item ->
                    new DocumentResponse(item.id(), item.title(), item.category(), item.date(), item.summary())).toList();
        } catch (RuntimeException exception) {
            return legacy.findAll();
        }
    }

    @Override
    public DocumentResponse findById(Long id) {
        if (!router.useUnified("DOCUMENT")) return legacy.findById(id);
        try {
            var item = unified.findById("DOCUMENT", "STATIC_DOCUMENTS", id);
            return new DocumentResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        } catch (RuntimeException exception) {
            return legacy.findById(id);
        }
    }
}
