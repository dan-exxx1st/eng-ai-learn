package com.devlingo.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewWordRequest(
    @NotNull UUID wordId,
    int quality  // 0-5: 0-1 = forgot, 2 = hard, 3 = ok, 4 = easy, 5 = perfect
) {}
