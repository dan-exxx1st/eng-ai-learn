package com.devlingo.web.dto;

public record AnswerResponse(
        boolean correct,
        String correctAnswer,
        String explanation,
        int answeredCount,
        int totalQuestions
) {}
