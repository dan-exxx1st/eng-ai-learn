package com.devlingo.web.dto;

public record ExerciseSubmitResponse(
        boolean correct,
        String correctAnswer,
        String explanation,
        String feedback
) {}
