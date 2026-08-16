package ye.gov.pmo.bootstrap.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public SupportRequestResponse create(@RequestBody SupportRequestRequest request) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<SupportRequestResponse> list() {
        return supportRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/requests/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public SupportRequestResponse updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        SupportRequest entity = supportRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Support request not found: " + id));

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
                entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "استفسار";
        }
        return switch (category.trim()) {
            case "استفسار", "اقتراح", "شكوى", "طلب وثيقة", "خدمة" -> category.trim();
            default -> category.trim();
        };
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
            default -> "new";
        };
    }

    public record SupportRequestRequest(
            String fullName,
            String email,
            String phone,
            String category,
            String subject,
            String message) {
    }

    public record StatusUpdateRequest(String status) {
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
            String createdAt) {
    }
}
