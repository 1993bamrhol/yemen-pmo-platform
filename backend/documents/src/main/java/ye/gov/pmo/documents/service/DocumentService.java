package ye.gov.pmo.documents.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.documents.dto.DocumentResponse;

@Service
public class DocumentService {

    private final List<DocumentResponse> documents = List.of(
            new DocumentResponse(
                    1L,
                    "وثيقة التحليل المؤسسي",
                    "وثيقة",
                    "16 أغسطس 2026",
                    "خريطة التنظيم والهيكل الرسمي للبوابة الحكومية."),
            new DocumentResponse(
                    2L,
                    "خطة MVP",
                    "خطة",
                    "14 أغسطس 2026",
                    "خارطة التنفيذ المرحلي للمنصة الرسمية."),
            new DocumentResponse(
                    3L,
                    "دليل الهوية البصرية",
                    "دليل",
                    "10 أغسطس 2026",
                    "إرشادات الألوان والخطوط والهوية الرقمية الرسمية."));

    public List<DocumentResponse> findAll() {
        return documents;
    }

    public DocumentResponse findById(Long id) {
        return documents.stream()
                .filter(document -> document.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found with id: " + id));
    }
}
