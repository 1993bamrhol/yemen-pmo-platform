package ye.gov.pmo.bootstrap.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.bootstrap.compatibility.ContentCompatibilityRouter;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin/content-compatibility/status")
public class ContentCompatibilityStatusController {
    private final ContentCompatibilityRouter router;

    public ContentCompatibilityStatusController(ContentCompatibilityRouter router) {
        this.router = router;
    }

    @GetMapping
    @PreAuthorize("@entityAuthorization.hasPlatformPermission('content.manage')")
    public ContentCompatibilityRouter.StatusReport status() {
        return router.status();
    }
}
