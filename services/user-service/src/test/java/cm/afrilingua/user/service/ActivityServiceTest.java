package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.Activity;
import cm.afrilingua.user.dto.ActivityType;
import cm.afrilingua.user.dto.LogActivityRequest;
import cm.afrilingua.user.entity.ActivityLog;
import cm.afrilingua.user.entity.UserProfile;
import cm.afrilingua.user.repository.ActivityLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private ActivityService activityService;

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID PROFILE_ID = UUID.randomUUID();

    private UserProfile profile() {
        return UserProfile.builder()
                .id(PROFILE_ID)
                .accountId(ACCOUNT_ID)
                .displayName("Malaika")
                .build();
    }

    @Test
    void logActivity_shouldPersistActivity_linkedToProfile() {
        when(userProfileService.getEntityByAccountId(ACCOUNT_ID)).thenReturn(profile());

        LogActivityRequest request = new LogActivityRequest(ActivityType.LESSON_COMPLETED)
                .metadata("lessonId=abc");

        Activity response = activityService.logActivity(ACCOUNT_ID, request);

        assertThat(response.getActivityType()).isEqualTo(ActivityType.LESSON_COMPLETED);
        assertThat(response.getMetadata()).isEqualTo("lessonId=abc");
        assertThat(response.getAccountId()).isEqualTo(ACCOUNT_ID);

        verify(activityLogRepository).save(any(ActivityLog.class));
    }

    @Test
    void listActivities_shouldQueryWithPageableLimit_notUnboundedList() {
        ActivityLog log = ActivityLog.builder()
                .id(UUID.randomUUID())
                .profile(profile())
                .activityType(ActivityLog.ActivityType.LESSON_COMPLETED)
                .build();

        when(userProfileService.getEntityByAccountId(ACCOUNT_ID)).thenReturn(profile());
        when(activityLogRepository.findByProfileIdOrderByOccurredAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(List.of(log));

        List<Activity> activities = activityService.listActivities(ACCOUNT_ID);

        assertThat(activities).hasSize(1);
        // Confirms the service goes through the paginated repository method
        // (capped at 50), not an unbounded findByProfileId.
        verify(activityLogRepository).findByProfileIdOrderByOccurredAtDesc(any(UUID.class), any(Pageable.class));
    }
}