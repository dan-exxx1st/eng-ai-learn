package com.devlingo.service;

import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.Role;
import com.devlingo.repository.UserRepository;
import com.devlingo.security.JwtTokenProvider;
import com.devlingo.web.dto.AuthResponse;
import com.devlingo.web.dto.LoginRequest;
import com.devlingo.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUser() {
        var request = new RegisterRequest("test@test.com", "password", "John", "Doe");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(tokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.email());
        assertEquals("access-token", response.accessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowIfEmailExists() {
        var request = new RegisterRequest("test@test.com", "password", null, null);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldReturnTokens() {
        var request = new LoginRequest("test@test.com", "password");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .passwordHash("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void login_shouldThrowIfInvalidCredentials() {
        var request = new LoginRequest("test@test.com", "wrong");
        User user = User.builder().email("test@test.com").passwordHash("encoded").role(Role.USER).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
