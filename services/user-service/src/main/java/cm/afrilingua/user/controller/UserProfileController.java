package cm.afrilingua.user.controller;

import cm.afrilingua.user.api.PreferencesApi;
import cm.afrilingua.user.api.UsersApi;
import cm.afrilingua.user.dto.*;
import cm.afrilingua.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserProfileController implements UsersApi, PreferencesApi {

    private final UserProfileService userProfileService;

    @Override
    public ResponseEntity<UserProfile> createUserProfile(CreateUserProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.create(request));
    }

    @Override
    public ResponseEntity<UserProfile> getUserProfile(UUID accountId) {
        return ResponseEntity.ok(userProfileService.getByAccountId(accountId));
    }

    @Override
    public ResponseEntity<UserProfile> updateUserProfile(UUID accountId, UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.update(accountId, request));
    }

    @Override
    public ResponseEntity<UserPreferences> getPreferences(UUID accountId) {
        return ResponseEntity.ok(userProfileService.getPreferences(accountId));
    }

    @Override
    public ResponseEntity<UserPreferences> updatePreferences(UUID accountId, UpdatePreferencesRequest request) {
        return ResponseEntity.ok(userProfileService.updatePreferences(accountId, request));
    }
}