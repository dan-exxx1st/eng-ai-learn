package com.devlingo.web.dto;

import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String exerciseType,
        String difficulty,
        String question,
        String options,
        int orderIndex
) {}
