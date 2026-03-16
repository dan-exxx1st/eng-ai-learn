package com.devlingo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "test-secret-key-for-tests-must-be-at-least-256-bits-long-enough",
                900000,
                604800000
        );
    }

    @Test
    void generateAccessToken_shouldCreateValidToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateAccessToken(userId, "test@example.com", "USER");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(userId, tokenProvider.getUserIdFromToken(token));
        assertEquals("test@example.com", tokenProvider.getEmailFromToken(token));
        assertEquals("USER", tokenProvider.getRoleFromToken(token));
        assertEquals("access", tokenProvider.getTokenType(token));
    }

    @Test
    void generateRefreshToken_shouldCreateRefreshType() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateRefreshToken(userId, "test@example.com", "USER");

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("refresh", tokenProvider.getTokenType(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid-token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
