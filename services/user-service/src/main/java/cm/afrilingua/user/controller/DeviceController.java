package cm.afrilingua.user.controller;

import cm.afrilingua.user.api.DevicesApi;
import cm.afrilingua.user.dto.Device;
import cm.afrilingua.user.dto.RegisterDeviceRequest;
import cm.afrilingua.user.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceController implements DevicesApi {

    private final DeviceService deviceService;

    @Override
    public ResponseEntity<Device> registerDevice(UUID accountId, RegisterDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.registerDevice(accountId, request));
    }

    @Override
    public ResponseEntity<List<Device>> listDevices(UUID accountId) {
        return ResponseEntity.ok(deviceService.listDevices(accountId));
    }
}