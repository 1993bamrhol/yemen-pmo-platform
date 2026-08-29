package ye.gov.pmo.bootstrap.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.bootstrap.backfill.BackfillReconciliationReport;
import ye.gov.pmo.bootstrap.backfill.ContentBackfillReconciliationService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin/content-backfill")
public class ContentBackfillController {
    private final ContentBackfillReconciliationService reconciliation;

    public ContentBackfillController(ContentBackfillReconciliationService reconciliation) {
        this.reconciliation = reconciliation;
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("@entityAuthorization.hasPlatformPermission('content.manage')")
    public BackfillReconciliationReport reconcile() {
        return reconciliation.reconcile();
    }
}
