package ye.gov.pmo.content.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContentWorkflowPolicyTest {

    private final ContentWorkflowPolicy policy = new ContentWorkflowPolicy();

    @Test
    void supportsTheApprovedPublicationPath() {
        assertEquals(ContentStatus.IN_REVIEW,
                policy.transition(ContentStatus.DRAFT, ContentAction.SUBMIT_REVIEW));
        assertEquals(ContentStatus.APPROVED,
                policy.transition(ContentStatus.IN_REVIEW, ContentAction.APPROVE));
        assertEquals(ContentStatus.PUBLISHED,
                policy.transition(ContentStatus.APPROVED, ContentAction.PUBLISH));
        assertEquals(ContentStatus.ARCHIVED,
                policy.transition(ContentStatus.PUBLISHED, ContentAction.ARCHIVE));
        assertEquals(ContentStatus.DRAFT,
                policy.transition(ContentStatus.ARCHIVED, ContentAction.RESTORE));
    }

    @Test
    void returnsContentToDraftWhenChangesAreRequested() {
        assertEquals(ContentStatus.DRAFT,
                policy.transition(ContentStatus.IN_REVIEW, ContentAction.REQUEST_CHANGES));
        assertEquals(ContentStatus.DRAFT,
                policy.transition(ContentStatus.APPROVED, ContentAction.REQUEST_CHANGES));
    }

    @Test
    void publishedContentCanSubmitANewRevisionWithoutChangingThePublicRevision() {
        assertEquals(ContentStatus.IN_REVIEW,
                policy.transition(ContentStatus.PUBLISHED, ContentAction.SUBMIT_REVIEW));
    }

    @Test
    void rejectsSkippedAndUnsupportedTransitions() {
        assertFalse(policy.canTransition(ContentStatus.DRAFT, ContentAction.PUBLISH));
        assertTrue(policy.canTransition(ContentStatus.APPROVED, ContentAction.PUBLISH));
        assertThrows(InvalidContentTransitionException.class,
                () -> policy.transition(ContentStatus.DRAFT, ContentAction.PUBLISH));
        assertThrows(InvalidContentTransitionException.class,
                () -> policy.transition(ContentStatus.PUBLISHED, ContentAction.RESTORE));
    }

    @Test
    void preventsRevisionAuthorFromApprovingOrPublishingWithoutBreakGlass() {
        assertThrows(ContentSeparationOfDutiesException.class,
                () -> policy.enforceSeparationOfDuties(ContentAction.APPROVE, 7L, 7L, false));
        assertThrows(ContentSeparationOfDutiesException.class,
                () -> policy.enforceSeparationOfDuties(ContentAction.PUBLISH, 7L, 7L, false));
    }

    @Test
    void permitsDifferentActorsAndAuditedPlatformBreakGlass() {
        policy.enforceSeparationOfDuties(ContentAction.APPROVE, 8L, 7L, false);
        policy.enforceSeparationOfDuties(ContentAction.PUBLISH, 7L, 7L, true);
    }
}
