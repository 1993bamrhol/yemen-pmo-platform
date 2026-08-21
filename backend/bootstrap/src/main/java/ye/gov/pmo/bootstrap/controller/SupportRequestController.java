package ye.gov.pmo.bootstrap.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ye.gov.pmo.bootstrap.entity.SupportRequest;
import ye.gov.pmo.bootstrap.repository.SupportRequestRepository;

@RestController
@RequestMapping("/api/support")
public class SupportRequestController {

    private final SupportRequestRepository supportRequestRepository;

    public SupportRequestController(SupportRequestRepository supportRequestRepository) {
        this.supportRequestRepository = supportRequestRepository;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportRequestResponse create(@Valid @RequestBody SupportRequestRequest request) {
        SupportRequest entity = new SupportRequest(
                request.fullName(),
                request.email(),
                request.phone(),
                normalizeCategory(request.category()),
                request.subject(),
                request.message());
        return toResponse(supportRequestRepository.save(entity));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('content.manage')")
    public List<SupportRequestResponse> list() {
        return supportRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/requests/{id}/status")
    @PreAuthorize("hasAuthority('content.manage')")
    public SupportRequestResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        SupportRequest entity = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Support request not found: " + id));

        entity.setStatus(normalizeStatus(request.status()));
        return toResponse(supportRequestRepository.save(entity));
    }

    private SupportRequestResponse toResponse(SupportRequest entity) {
        return new SupportRequestResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCategory(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt());
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "استفسار";
        }
        String normalized = category.trim();
        Set<String> allowedCategories = Set.of("استفسار", "اقتراح", "شكوى", "طلب وثيقة", "خدمة");
        if (!allowedCategories.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported support request category");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "new";
        }
        return switch (status.trim().toLowerCase()) {
            case "new", "جديدة" -> "new";
            case "in_review", "review", "قيد_المراجعة", "قيد المراجعة" -> "in_review";
            case "replied", "answered", "تم_الرد", "تم الرد" -> "replied";
            case "resolved", "closed", "مغلقة", "مقفل" -> "resolved";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported support request status");
        };
    }

    public record SupportRequestRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 120) String email,
            @Size(max = 30) String phone,
            @NotBlank @Size(max = 60) String category,
            @NotBlank @Size(max = 180) String subject,
            @NotBlank @Size(max = 5000) String message) {
    }

    public record StatusUpdateRequest(@NotBlank @Size(max = 30) String status) {
    }

    public record SupportRequestResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String category,
            String subject,
            String message,
            String status,
            LocalDateTime createdAt) {
    }
}
