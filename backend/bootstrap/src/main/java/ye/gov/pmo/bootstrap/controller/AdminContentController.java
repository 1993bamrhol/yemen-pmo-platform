package ye.gov.pmo.bootstrap.controller;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.bootstrap.entity.ContentItem;
import ye.gov.pmo.bootstrap.repository.AdminContentRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    private final AdminContentRepository contentRepository;

    public AdminContentController(AdminContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @GetMapping("/content")
    public List<ContentItemResponse> getContent() {
        return contentRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
               .map(this::toResponse)
               .toList();
    }

    @GetMapping("/content/summary")
    public ContentSummary getSummary() {
        List<ContentItem> items = contentRepository.findAll();
        long total = items.size();
        long published = items.stream().filter(item -> "منشور".equals(item.getStatus())).count();
        long draft = items.stream().filter(item -> "مسودة".equals(item.getStatus()) || "قيد المراجعة".equals(item.getStatus())).count();
        long archived = items.stream().filter(item -> "مؤرشف".equals(item.getStatus())).count();
        return new ContentSummary(total, published, draft, archived);
    }

    @GetMapping("/content/{id}")
    public ContentItemResponse getById(@PathVariable("id") Long id) {
        return toResponse(contentRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content item not found with id: " + id)));
    }

    @PostMapping("/content")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentItemResponse create(@RequestBody ContentRequest request) {
        ContentItem item = new ContentItem(
               normalizeType(request.type()),
               request.title(),
               normalizeStatus(request.status()),
               request.author(),
               request.category());
        return toResponse(contentRepository.save(item));
    }

    @PutMapping("/content/{id}")
    public ContentItemResponse update(@PathVariable("id") Long id, @RequestBody ContentRequest request) {
        ContentItem existing = contentRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content item not found with id: " + id));

        existing.setType(normalizeType(request.type()));
        existing.setTitle(request.title());
        existing.setStatus(normalizeStatus(request.status()));
        existing.setAuthor(request.author());
        existing.setCategory(request.category());

        return toResponse(contentRepository.save(existing));
    }

    @DeleteMapping("/content/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!contentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content item not found with id: " + id);
        }
        contentRepository.deleteById(id);
    }

    private ContentItemResponse toResponse(ContentItem item) {
        return new ContentItemResponse(
               item.getId(),
               item.getType(),
               item.getTitle(),
               item.getStatus(),
               item.getAuthor(),
               item.getCategory(),
               item.getUpdatedAt() == null ? null : item.getUpdatedAt().toString());
    }

    private String normalizeType(String type) {
        return type == null || type.isBlank() ? "news" : type.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "مسودة";
        }
        return switch (status.trim()) {
            case "published", "منشور" -> "منشور";
            case "draft", "مسودة" -> "مسودة";
            case "review", "قيد المراجعة" -> "قيد المراجعة";
            case "archived", "مؤرشف" -> "مؤرشف";
            default -> status.trim();
        };
    }

    public record ContentRequest(
            String type,
            String title,
            String status,
            String author,
            String category) {
    }

    public record ContentItemResponse(
            Long id,
            String type,
            String title,
            String status,
            String author,
            String category,
            String updatedAt) {
    }

    public record ContentSummary(
            long total,
            long published,
            long draft,
            long archived) {
    }
}
