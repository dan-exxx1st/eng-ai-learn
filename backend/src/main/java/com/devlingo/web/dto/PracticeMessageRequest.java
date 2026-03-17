package com.devlingo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PracticeMessageRequest(
        @NotNull UUID sessionId,
        @NotBlank String message
) {}
