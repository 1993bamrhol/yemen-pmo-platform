package ye.gov.pmo.decisions.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.decisions.dto.DecisionResponse;

@Service
public class DecisionService implements DecisionQuery {

    private final List<DecisionResponse> decisions = List.of(
            new DecisionResponse(
                    1L,
                    "قرار اعتماد الهوية البصرية الرسمية",
                    "قرار",
                    "12 أغسطس 2026",
                    "اعتماد الألوان والخطوط والطابع الرسمي للبوابة."),
            new DecisionResponse(
                    2L,
                    "تعميم تنظيم المحتوى الحكومي",
                    "تعميم",
                    "09 أغسطس 2026",
                    "تحديد أسلوب النشر والتصنيف والأرشفة."),
            new DecisionResponse(
                    3L,
                    "تنظيم آلية التفاعل مع المواطنين",
                    "توجيه",
                    "05 أغسطس 2026",
                    "ضبط آلية الاستفسارات والملاحظات عبر القنوات الرسمية."));

    @Override
    public List<DecisionResponse> findAll() {
        return decisions;
    }

    @Override
    public DecisionResponse findById(Long id) {
        return decisions.stream()
                .filter(decision -> decision.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found with id: " + id));
    }
}
