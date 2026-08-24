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
    private final ContentCompatibilityExecutor executor;

    public DecisionCompatibilityQuery(DecisionService legacy, UnifiedLegacyProjectionService unified,
                                      ContentCompatibilityExecutor executor) {
        this.legacy = legacy;
        this.unified = unified;
        this.executor = executor;
    }

    @Override
    public List<DecisionResponse> findAll() {
        return executor.execute("DECISION", "list", legacy::findAll, () ->
                unified.findAll("DECISION", "STATIC_DECISIONS").stream().map(item ->
                        new DecisionResponse(item.id(), item.title(), item.category(), item.date(), item.summary()))
                        .toList());
    }

    @Override
    public DecisionResponse findById(Long id) {
        DecisionResponse legacyItem = legacy.findById(id);
        return executor.execute("DECISION", "detail", () -> legacyItem, () -> {
            var item = unified.findById("DECISION", "STATIC_DECISIONS", id);
            return new DecisionResponse(item.id(), item.title(), item.category(), item.date(), item.summary());
        });
    }
}
