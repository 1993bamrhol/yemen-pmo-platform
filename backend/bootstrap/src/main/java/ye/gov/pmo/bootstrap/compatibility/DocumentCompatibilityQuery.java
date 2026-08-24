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
    private final ContentCompatibilityExecutor executor;

    public DocumentCompatibilityQuery(DocumentService legacy, UnifiedLegacyProjectionService unified,
                                      ContentCompatibilityExecutor executor) {
        this.legacy = legacy;
        this.unified = unified;
        this.executor = executor;
    }

    @Override
    public List<DocumentResponse> findAll() {
        return executor.execute("DOCUMENT", "list", legacy::findAll, () ->
                unified.findAll("DOCUMENT", "STATIC_DOCUMENTS").stream().map(item ->
                        new DocumentResponse(item.id(), item.title(), item.category(), item.date(), item.summary()))
                        .toList());
    }

    @Override
    public DocumentResponse findById(Long id) {
        DocumentResponse legacyItem = legacy.findById(id);
        return executor.execute("DOCUMENT", "detail", () -> legacyItem, () -> {
            var item = unified.findById("DOCUMENT", "STATIC_DOCUMENTS", id);
            return new DocumentResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        });
    }
}
