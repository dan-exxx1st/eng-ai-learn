package com.devlingo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExerciseSubmitRequest(
        @NotNull UUID exerciseId,
        @NotBlank String answer
) {}
