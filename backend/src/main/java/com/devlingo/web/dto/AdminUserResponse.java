package com.devlingo.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String languageLevel,
        String specialization,
        boolean blocked,
        LocalDateTime createdAt
) {}
