package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.Device;
import cm.afrilingua.user.dto.DeviceType;
import cm.afrilingua.user.dto.RegisterDeviceRequest;
import cm.afrilingua.user.entity.UserDevice;
import cm.afrilingua.user.entity.UserProfile;
import cm.afrilingua.user.repository.UserDeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private UserDeviceRepository deviceRepository;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private DeviceService deviceService;

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
    void registerDevice_shouldCreateNewDevice_whenNotPreviouslyRegistered() {
        when(userProfileService.getEntityByAccountId(ACCOUNT_ID)).thenReturn(profile());
        when(deviceRepository.findByProfileIdAndDeviceId(PROFILE_ID, "phone-001"))
                .thenReturn(Optional.empty());

        RegisterDeviceRequest request = new RegisterDeviceRequest("phone-001", DeviceType.ANDROID);
        Device response = deviceService.registerDevice(ACCOUNT_ID, request);

        assertThat(response.getDeviceId()).isEqualTo("phone-001");
        assertThat(response.getDeviceType()).isEqualTo(DeviceType.ANDROID);
        assertThat(response.getAccountId()).isEqualTo(ACCOUNT_ID);

        verify(deviceRepository).save(any(UserDevice.class));
    }

    @Test
    void registerDevice_shouldUpdateExistingDevice_ratherThanDuplicating() {
        UserDevice existing = UserDevice.builder()
                .id(UUID.randomUUID())
                .profile(profile())
                .deviceId("phone-001")
                .deviceType(UserDevice.DeviceType.ANDROID)
                .build();

        when(userProfileService.getEntityByAccountId(ACCOUNT_ID)).thenReturn(profile());
        when(deviceRepository.findByProfileIdAndDeviceId(PROFILE_ID, "phone-001"))
                .thenReturn(Optional.of(existing));

        RegisterDeviceRequest request = new RegisterDeviceRequest("phone-001", DeviceType.IOS);
        Device response = deviceService.registerDevice(ACCOUNT_ID, request);

        // Same underlying id, deviceType updated -- this is an upsert, not an insert
        assertThat(response.getId()).isEqualTo(existing.getId());
        assertThat(response.getDeviceType()).isEqualTo(DeviceType.IOS);

        verify(deviceRepository, times(1)).save(existing);
    }

    @Test
    void listDevices_shouldReturnAllDevicesForProfile() {
        UserDevice device = UserDevice.builder()
                .id(UUID.randomUUID())
                .profile(profile())
                .deviceId("phone-001")
                .deviceType(UserDevice.DeviceType.ANDROID)
                .build();

        when(userProfileService.getEntityByAccountId(ACCOUNT_ID)).thenReturn(profile());
        when(deviceRepository.findByProfileId(PROFILE_ID)).thenReturn(List.of(device));

        List<Device> devices = deviceService.listDevices(ACCOUNT_ID);

        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getDeviceId()).isEqualTo("phone-001");
    }
}