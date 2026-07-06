package cm.afrilingua.auth.service;

import cm.afrilingua.auth.dto.AuthResponse;
import cm.afrilingua.auth.dto.LoginRequest;
import cm.afrilingua.auth.dto.RegisterRequest;
import cm.afrilingua.auth.entity.User;
import cm.afrilingua.auth.exception.EmailAlreadyInUseException;
import cm.afrilingua.auth.exception.InvalidCredentialsException;
import cm.afrilingua.auth.repository.UserRepository;
import cm.afrilingua.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "user@afrilingua.cm";
    private static final String RAW_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "hashed_password";

    @Test
    void register_shouldCreateUser_whenEmailIsNotTaken() {
        RegisterRequest request = new RegisterRequest().email(EMAIL).password(RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(jwtService.generateAccessToken(any(), eq(EMAIL), eq("USER"))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), eq(EMAIL), eq("USER"))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo(EMAIL);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals(EMAIL) &&
                user.getPasswordHash().equals(HASHED_PASSWORD) &&
                user.getRole() == User.Role.USER
        ));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest().email(EMAIL).password(RAW_PASSWORD);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest().email(EMAIL).password(RAW_PASSWORD);
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtService.generateAccessToken(any(), eq(EMAIL), eq("USER"))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), eq(EMAIL), eq("USER"))).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        LoginRequest request = new LoginRequest().email(EMAIL).password(RAW_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrow_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest().email(EMAIL).password("wrong-password");
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", HASHED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
