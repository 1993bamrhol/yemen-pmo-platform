package ye.gov.pmo.decisions.service;

import java.util.List;
import ye.gov.pmo.decisions.dto.DecisionResponse;

public interface DecisionQuery {
    List<DecisionResponse> findAll();

    DecisionResponse findById(Long id);
}
