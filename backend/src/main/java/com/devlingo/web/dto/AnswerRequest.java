package com.devlingo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnswerRequest(
        @NotNull UUID testId,
        int questionIndex,
        @NotBlank String answer
) {}
