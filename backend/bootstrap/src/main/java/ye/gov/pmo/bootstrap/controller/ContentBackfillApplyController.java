package ye.gov.pmo.bootstrap.controller;

import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.bootstrap.backfill.BackfillApplyRequest;
import ye.gov.pmo.bootstrap.backfill.BackfillApplyResponse;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillApplyService;

@RestController
@RequestMapping("/api/v1/admin/content-backfill")
@ConditionalOnProperty(name = "features.unified-content-backfill-apply.enabled", havingValue = "true")
public class ContentBackfillApplyController {
    private final ContentBackfillApplyService applyService;

    public ContentBackfillApplyController(ContentBackfillApplyService applyService) {
        this.applyService = applyService;
    }

    @PostMapping("/apply")
    @PreAuthorize("@entityAuthorization.hasPlatformPermission('content.manage')")
    public BackfillApplyResponse apply(@Valid @RequestBody BackfillApplyRequest request) {
        return applyService.apply(request);
    }
}
