package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.Activity;
import cm.afrilingua.user.dto.LogActivityRequest;
import cm.afrilingua.user.entity.ActivityLog;
import cm.afrilingua.user.entity.UserProfile;
import cm.afrilingua.user.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final int RECENT_ACTIVITY_LIMIT = 50;

    private final ActivityLogRepository activityLogRepository;
    private final UserProfileService userProfileService;

    @Transactional
    public Activity logActivity(UUID accountId, LogActivityRequest request) {
        UserProfile profile = userProfileService.getEntityByAccountId(accountId);

        ActivityLog activity = ActivityLog.builder()
                .profile(profile)
                .activityType(ActivityLog.ActivityType.valueOf(request.getActivityType().getValue()))
                .metadata(request.getMetadata())
                .build();

        activityLogRepository.save(activity);
        return toDto(activity, accountId);
    }

    @Transactional(readOnly = true)
    public List<Activity> listActivities(UUID accountId) {
        UserProfile profile = userProfileService.getEntityByAccountId(accountId);
        return activityLogRepository
                .findByProfileIdOrderByOccurredAtDesc(profile.getId(), PageRequest.of(0, RECENT_ACTIVITY_LIMIT))
                .stream()
                .map(activity -> toDto(activity, accountId))
                .collect(Collectors.toList());
    }

    private Activity toDto(ActivityLog activity, UUID accountId) {
        return new Activity()
                .id(activity.getId())
                .accountId(accountId)
                .activityType(cm.afrilingua.user.dto.ActivityType.valueOf(activity.getActivityType().name()))
                .occurredAt(activity.getOccurredAt().atOffset(ZoneOffset.UTC))
                .metadata(activity.getMetadata());
    }
}