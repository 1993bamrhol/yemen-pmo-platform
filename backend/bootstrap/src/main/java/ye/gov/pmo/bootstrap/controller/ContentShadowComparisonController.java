package ye.gov.pmo.bootstrap.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonReport;
import ye.gov.pmo.bootstrap.shadow.ContentShadowComparisonService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin/content-shadow-comparison")
public class ContentShadowComparisonController {
    private final ContentShadowComparisonService comparisonService;

    public ContentShadowComparisonController(ContentShadowComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping
    @PreAuthorize("@entityAuthorization.hasPlatformPermission('content.manage')")
    public ContentShadowComparisonReport compare() {
        return comparisonService.compare();
    }
}
