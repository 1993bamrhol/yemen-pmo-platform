package ye.gov.pmo.bootstrap.compatibility;

import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ye.gov.pmo.decisions.dto.DecisionResponse;
import ye.gov.pmo.decisions.service.DecisionQuery;
import ye.gov.pmo.decisions.service.DecisionService;

@Service
@Primary
public class DecisionCompatibilityQuery implements DecisionQuery {
    private final DecisionService legacy;
    private final UnifiedLegacyProjectionService unified;
    private final ContentCompatibilityRouter router;

    public DecisionCompatibilityQuery(DecisionService legacy, UnifiedLegacyProjectionService unified,
                                      ContentCompatibilityRouter router) {
        this.legacy = legacy;
        this.unified = unified;
        this.router = router;
    }

    @Override
    public List<DecisionResponse> findAll() {
        if (!router.useUnified("DECISION")) return legacy.findAll();
        try {
            return unified.findAll("DECISION", "STATIC_DECISIONS").stream().map(item ->
                    new DecisionResponse(item.id(), item.title(), item.category(), item.date(), item.summary())).toList();
        } catch (RuntimeException exception) {
            return legacy.findAll();
        }
    }

    @Override
    public DecisionResponse findById(Long id) {
        if (!router.useUnified("DECISION")) return legacy.findById(id);
        try {
            var item = unified.findById("DECISION", "STATIC_DECISIONS", id);
            return new DecisionResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        } catch (RuntimeException exception) {
            return legacy.findById(id);
        }
    }
}
