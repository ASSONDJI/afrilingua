package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.CreateUserProfileRequest;
import cm.afrilingua.user.dto.UpdatePreferencesRequest;
import cm.afrilingua.user.dto.UpdateUserProfileRequest;
import cm.afrilingua.user.dto.UserPreferences;
import cm.afrilingua.user.dto.UserProfile;
import cm.afrilingua.user.exception.ProfileAlreadyExistsException;
import cm.afrilingua.user.exception.ProfileNotFoundException;
import cm.afrilingua.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    @Transactional
    public UserProfile create(CreateUserProfileRequest request) {
        if (profileRepository.existsByAccountId(request.getAccountId())) {
            throw new ProfileAlreadyExistsException(request.getAccountId());
        }

        cm.afrilingua.user.entity.UserProfile profile = cm.afrilingua.user.entity.UserProfile.builder()
                .accountId(request.getAccountId())
                .displayName(request.getDisplayName())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .build();

        profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile getByAccountId(UUID accountId) {
        return toDto(getEntityByAccountId(accountId));
    }

    @Transactional
    public UserProfile update(UUID accountId, UpdateUserProfileRequest request) {
        cm.afrilingua.user.entity.UserProfile profile = getEntityByAccountId(accountId);

        // Partial update: only overwrite fields explicitly provided by the client
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        return toDto(profile);
    }

    @Transactional(readOnly = true)
    public UserPreferences getPreferences(UUID accountId) {
        cm.afrilingua.user.entity.UserProfile profile = getEntityByAccountId(accountId);
        return new UserPreferences()
                .accountId(profile.getAccountId())
                .learningLanguages(new ArrayList<>(profile.getLearningLanguages()));
    }

    @Transactional
    public UserPreferences updatePreferences(UUID accountId, UpdatePreferencesRequest request) {
        cm.afrilingua.user.entity.UserProfile profile = getEntityByAccountId(accountId);

        // Full replacement, not a merge: mutate the managed collection in place
        // rather than reassigning it, so Hibernate's orphanRemoval-equivalent
        // delete/insert on the element collection table behaves correctly.
        profile.getLearningLanguages().clear();
        profile.getLearningLanguages().addAll(request.getLearningLanguages());

        return new UserPreferences()
                .accountId(profile.getAccountId())
                .learningLanguages(new ArrayList<>(profile.getLearningLanguages()));
    }

    /** Used by DeviceService and ActivityService to resolve the profile behind an accountId. */
    cm.afrilingua.user.entity.UserProfile getEntityByAccountId(UUID accountId) {
        return profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));
    }

    private UserProfile toDto(cm.afrilingua.user.entity.UserProfile profile) {
        return new UserProfile()
                .id(profile.getId())
                .accountId(profile.getAccountId())
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
    }
}