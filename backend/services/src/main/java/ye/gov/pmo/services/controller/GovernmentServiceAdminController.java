package ye.gov.pmo.services.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ye.gov.pmo.services.dto.GovernmentServiceAdminResponse;
import ye.gov.pmo.services.dto.GovernmentServiceRequest;
import ye.gov.pmo.services.dto.ServicePublicationRequest;
import ye.gov.pmo.services.dto.ServiceVerificationRequest;
import ye.gov.pmo.services.service.GovernmentServiceService;
import ye.gov.pmo.shared.web.ApiV1;

@RestController
@ApiV1
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("isAuthenticated()")
public class GovernmentServiceAdminController {

    private final GovernmentServiceService service;

    public GovernmentServiceAdminController(GovernmentServiceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GovernmentServiceAdminResponse create(
            @Valid @RequestBody GovernmentServiceRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.create(request, correlationId);
    }

    @PutMapping("/{id}")
    public GovernmentServiceAdminResponse update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody GovernmentServiceRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.update(id, request, correlationId);
    }

    @PutMapping("/{id}/publication")
    public GovernmentServiceAdminResponse updatePublication(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ServicePublicationRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.updatePublication(id, request, correlationId);
    }

    @PutMapping("/{id}/verification")
    public GovernmentServiceAdminResponse updateVerification(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ServiceVerificationRequest request,
            @RequestHeader(name = "X-Correlation-ID", required = false) String correlationId) {
        return service.updateVerification(id, request, correlationId);
    }
}
