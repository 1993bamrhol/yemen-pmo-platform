package ye.gov.pmo.bootstrap.compatibility;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog;
import ye.gov.pmo.bootstrap.backfill.LegacyContentSourceCatalog.Snapshot;
import ye.gov.pmo.content.dto.PublicContentResponse;
import ye.gov.pmo.content.service.PublicContentService;

@Service
@Transactional(readOnly = true)
public class UnifiedLegacyProjectionService {
    private final PublicContentService publicContent;
    private final LegacyContentSourceCatalog legacyCatalog;
    private final JdbcTemplate jdbcTemplate;

    public UnifiedLegacyProjectionService(PublicContentService publicContent,
                                          LegacyContentSourceCatalog legacyCatalog,
                                          JdbcTemplate jdbcTemplate) {
        this.publicContent = publicContent;
        this.legacyCatalog = legacyCatalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProjectedLegacyContent> findAll(String contentType, String sourceSystem) {
        Map<UUID, Snapshot> snapshotsByContentId = snapshots(contentType, sourceSystem).stream()
                .collect(Collectors.toMap(this::mappedId, Function.identity()));
        return publicContent.findPublishedForCompatibility(contentType, 0, 100).items().stream()
                .map(item -> project(item, requiredSnapshot(snapshotsByContentId, item.id())))
                .toList();
    }

    public ProjectedLegacyContent findById(String contentType, String sourceSystem, long legacyId) {
        Snapshot snapshot = snapshots(contentType, sourceSystem).stream()
                .filter(item -> item.legacyId() == legacyId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Legacy source mapping is missing"));
        return project(publicContent.findByIdForCompatibility(mappedId(snapshot)), snapshot);
    }

    private List<Snapshot> snapshots(String contentType, String sourceSystem) {
        return legacyCatalog.discover().stream()
                .filter(item -> item.sourceType().equals(contentType) && item.sourceSystem().equals(sourceSystem))
                .toList();
    }

    private UUID mappedId(Snapshot snapshot) {
        List<UUID> ids = jdbcTemplate.query("""
                select content_item_id from legacy_content_mappings
                where source_system = ? and source_type = ? and legacy_id = ?
                """, (resultSet, row) -> resultSet.getObject(1, UUID.class),
                snapshot.sourceSystem(), snapshot.sourceType(), snapshot.legacyId());
        if (ids.size() != 1) {
            throw new IllegalStateException("Expected one legacy content mapping for " + snapshot.sourceKey());
        }
        return ids.getFirst();
    }

    private Snapshot requiredSnapshot(Map<UUID, Snapshot> snapshots, UUID contentItemId) {
        Snapshot snapshot = snapshots.get(contentItemId);
        if (snapshot == null) {
            throw new IllegalStateException("Unified item has no compatible legacy projection");
        }
        return snapshot;
    }

    private ProjectedLegacyContent project(PublicContentResponse item, Snapshot snapshot) {
        return new ProjectedLegacyContent(snapshot.legacyId(), item.title(), snapshot.category(),
                snapshot.sourceDate(), item.summary());
    }

    public record ProjectedLegacyContent(
            long id,
            String title,
            String category,
            String date,
            String summary) {
    }
}
