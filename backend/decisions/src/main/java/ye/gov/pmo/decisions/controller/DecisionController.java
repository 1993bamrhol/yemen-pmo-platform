package ye.gov.pmo.decisions.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.decisions.dto.DecisionResponse;
import ye.gov.pmo.decisions.service.DecisionQuery;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionQuery decisionService;

    public DecisionController(DecisionQuery decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping
    public List<DecisionResponse> findAll() {
        return decisionService.findAll();
    }

    @GetMapping("/{id}")
    public DecisionResponse findById(@PathVariable("id") Long id) {
        return decisionService.findById(id);
    }
}
