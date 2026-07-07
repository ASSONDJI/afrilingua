package cm.afrilingua.user.controller;

import cm.afrilingua.user.api.ActivitiesApi;
import cm.afrilingua.user.dto.Activity;
import cm.afrilingua.user.dto.LogActivityRequest;
import cm.afrilingua.user.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivityController implements ActivitiesApi {

    private final ActivityService activityService;

    @Override
    public ResponseEntity<Activity> logActivity(UUID accountId, LogActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.logActivity(accountId, request));
    }

    @Override
    public ResponseEntity<List<Activity>> listActivities(UUID accountId) {
        return ResponseEntity.ok(activityService.listActivities(accountId));
    }
}