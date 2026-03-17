package com.devlingo.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddWordRequest(
        @NotBlank String word,
        String translation,
        String context,
        String source
) {}
