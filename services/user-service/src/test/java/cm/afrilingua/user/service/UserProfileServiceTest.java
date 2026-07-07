package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.CreateUserProfileRequest;
import cm.afrilingua.user.dto.UpdatePreferencesRequest;
import cm.afrilingua.user.dto.UpdateUserProfileRequest;
import cm.afrilingua.user.dto.UserPreferences;
import cm.afrilingua.user.dto.UserProfile;
import cm.afrilingua.user.exception.ProfileAlreadyExistsException;
import cm.afrilingua.user.exception.ProfileNotFoundException;
import cm.afrilingua.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String DISPLAY_NAME = "Malaika";
    private static final String BIO = "Apprenante de yemba";

    @Test
    void create_shouldSaveProfile_whenAccountIdIsNotTaken() {
        CreateUserProfileRequest request = new CreateUserProfileRequest()
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .bio(BIO);

        when(profileRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(false);

        UserProfile response = userProfileService.create(request);

        assertThat(response.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.getBio()).isEqualTo(BIO);

        verify(profileRepository).save(any(cm.afrilingua.user.entity.UserProfile.class));
    }

    @Test
    void create_shouldThrow_whenAccountIdAlreadyHasProfile() {
        CreateUserProfileRequest request = new CreateUserProfileRequest()
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME);

        when(profileRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(true);

        assertThatThrownBy(() -> userProfileService.create(request))
                .isInstanceOf(ProfileAlreadyExistsException.class);

        verify(profileRepository, never()).save(any());
    }

    @Test
    void getByAccountId_shouldReturnProfile_whenExists() {
        cm.afrilingua.user.entity.UserProfile entity = cm.afrilingua.user.entity.UserProfile.builder()
                .id(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .bio(BIO)
                .build();

        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        UserProfile response = userProfileService.getByAccountId(ACCOUNT_ID);

        assertThat(response.getDisplayName()).isEqualTo(DISPLAY_NAME);
    }

    @Test
    void getByAccountId_shouldThrow_whenNotFound() {
        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getByAccountId(ACCOUNT_ID))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void update_shouldOnlyOverwriteProvidedFields() {
        cm.afrilingua.user.entity.UserProfile entity = cm.afrilingua.user.entity.UserProfile.builder()
                .id(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .bio(BIO)
                .build();

        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest()
                .avatarUrl("https://example.com/avatar.png");

        UserProfile response = userProfileService.update(ACCOUNT_ID, request);

        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        // Fields not present in the request must remain untouched
        assertThat(response.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.getBio()).isEqualTo(BIO);
    }

    @Test
    void update_shouldThrow_whenProfileNotFound() {
        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());

        UpdateUserProfileRequest request = new UpdateUserProfileRequest().displayName("New name");

        assertThatThrownBy(() -> userProfileService.update(ACCOUNT_ID, request))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void getPreferences_shouldReturnLearningLanguages() {
        cm.afrilingua.user.entity.UserProfile entity = cm.afrilingua.user.entity.UserProfile.builder()
                .id(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .learningLanguages(new java.util.ArrayList<>(List.of("yem", "dua")))
                .build();

        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        UserPreferences preferences = userProfileService.getPreferences(ACCOUNT_ID);

        assertThat(preferences.getLearningLanguages()).containsExactlyInAnyOrder("yem", "dua");
    }

    @Test
    void updatePreferences_shouldFullyReplaceLearningLanguages() {
        cm.afrilingua.user.entity.UserProfile entity = cm.afrilingua.user.entity.UserProfile.builder()
                .id(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .learningLanguages(new java.util.ArrayList<>(List.of("yem")))
                .build();

        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        UpdatePreferencesRequest request = new UpdatePreferencesRequest()
                .learningLanguages(List.of("dua", "bas"));

        UserPreferences result = userProfileService.updatePreferences(ACCOUNT_ID, request);

        // Full replacement: "yem" must be gone, not merged in
        assertThat(result.getLearningLanguages()).containsExactlyInAnyOrder("dua", "bas");
        assertThat(result.getLearningLanguages()).doesNotContain("yem");
    }

    @Test
    void getEntityByAccountId_shouldReturnEntity_whenExists() {
        cm.afrilingua.user.entity.UserProfile entity = cm.afrilingua.user.entity.UserProfile.builder()
                .id(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .displayName(DISPLAY_NAME)
                .build();

        when(profileRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(entity));

        cm.afrilingua.user.entity.UserProfile result = userProfileService.getEntityByAccountId(ACCOUNT_ID);

        assertThat(result).isEqualTo(entity);
    }
}