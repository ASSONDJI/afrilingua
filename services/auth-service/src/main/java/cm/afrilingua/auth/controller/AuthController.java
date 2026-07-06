package cm.afrilingua.auth.controller;

import cm.afrilingua.auth.api.AuthApi;
import cm.afrilingua.auth.dto.AuthResponse;
import cm.afrilingua.auth.dto.LoginRequest;
import cm.afrilingua.auth.dto.RefreshRequest;
import cm.afrilingua.auth.dto.RegisterRequest;
import cm.afrilingua.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(RefreshRequest refreshRequest) {
        AuthResponse response = authService.refreshToken(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}