package cm.afrilingua.user.service;

import cm.afrilingua.user.dto.Device;
import cm.afrilingua.user.dto.RegisterDeviceRequest;
import cm.afrilingua.user.entity.UserDevice;
import cm.afrilingua.user.entity.UserProfile;
import cm.afrilingua.user.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UserDeviceRepository deviceRepository;
    private final UserProfileService userProfileService;

    @Transactional
    public Device registerDevice(UUID accountId, RegisterDeviceRequest request) {
        UserProfile profile = userProfileService.getEntityByAccountId(accountId);

        // Upsert: a device re-registering (e.g. app reopened) updates its type
        // and last-seen timestamp rather than creating a duplicate row.
        UserDevice device = deviceRepository.findByProfileIdAndDeviceId(profile.getId(), request.getDeviceId())
                .orElseGet(() -> UserDevice.builder()
                        .profile(profile)
                        .deviceId(request.getDeviceId())
                        .build());

        device.setDeviceType(UserDevice.DeviceType.valueOf(request.getDeviceType().getValue()));
        device.setLastActiveAt(Instant.now());

        deviceRepository.save(device);
        return toDto(device, accountId);
    }

    @Transactional(readOnly = true)
    public List<Device> listDevices(UUID accountId) {
        UserProfile profile = userProfileService.getEntityByAccountId(accountId);
        return deviceRepository.findByProfileId(profile.getId()).stream()
                .map(device -> toDto(device, accountId))
                .collect(Collectors.toList());
    }

    private Device toDto(UserDevice device, UUID accountId) {
        return new Device()
                .id(device.getId())
                .accountId(accountId)
                .deviceId(device.getDeviceId())
                .deviceType(cm.afrilingua.user.dto.DeviceType.valueOf(device.getDeviceType().name()))
                .lastActiveAt(device.getLastActiveAt().atOffset(ZoneOffset.UTC));
    }
}