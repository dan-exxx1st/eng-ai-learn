package com.devlingo.web.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String role,
        String accessToken,
        String refreshToken
) {}
