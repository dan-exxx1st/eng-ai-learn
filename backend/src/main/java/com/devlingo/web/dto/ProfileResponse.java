package com.devlingo.web.dto;

import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String languageLevel,
        String specialization
) {}
