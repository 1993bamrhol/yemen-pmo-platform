package ye.gov.pmo.content.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ContentWorkflowPolicy {

    private static final Map<ContentStatus, Map<ContentAction, ContentStatus>> TRANSITIONS = transitions();

    public ContentStatus transition(ContentStatus currentStatus, ContentAction action) {
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(action, "action must not be null");
        ContentStatus target = TRANSITIONS.getOrDefault(currentStatus, Map.of()).get(action);
        if (target == null) {
            throw new InvalidContentTransitionException(currentStatus, action);
        }
        return target;
    }

    public boolean canTransition(ContentStatus currentStatus, ContentAction action) {
        return currentStatus != null
                && action != null
                && TRANSITIONS.getOrDefault(currentStatus, Map.of()).containsKey(action);
    }

    public void enforceSeparationOfDuties(ContentAction action, Long actorUserId,
                                          Long revisionCreatedBy, boolean platformBreakGlass) {
        if ((action == ContentAction.APPROVE || action == ContentAction.PUBLISH)
                && actorUserId != null
                && actorUserId.equals(revisionCreatedBy)
                && !platformBreakGlass) {
            throw new ContentSeparationOfDutiesException(action);
        }
    }

    private static Map<ContentStatus, Map<ContentAction, ContentStatus>> transitions() {
        EnumMap<ContentStatus, Map<ContentAction, ContentStatus>> result =
                new EnumMap<>(ContentStatus.class);
        result.put(ContentStatus.DRAFT, Map.of(
                ContentAction.SUBMIT_REVIEW, ContentStatus.IN_REVIEW));
        result.put(ContentStatus.IN_REVIEW, Map.of(
                ContentAction.REQUEST_CHANGES, ContentStatus.DRAFT,
                ContentAction.APPROVE, ContentStatus.APPROVED));
        result.put(ContentStatus.APPROVED, Map.of(
                ContentAction.REQUEST_CHANGES, ContentStatus.DRAFT,
                ContentAction.PUBLISH, ContentStatus.PUBLISHED));
        result.put(ContentStatus.PUBLISHED, Map.of(
                ContentAction.ARCHIVE, ContentStatus.ARCHIVED,
                ContentAction.SUBMIT_REVIEW, ContentStatus.IN_REVIEW));
        result.put(ContentStatus.ARCHIVED, Map.of(
                ContentAction.RESTORE, ContentStatus.DRAFT));
        return Map.copyOf(result);
    }
}
