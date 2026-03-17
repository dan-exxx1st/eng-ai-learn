package com.devlingo.web.dto;

import java.util.List;
import java.util.UUID;

public record TestResultResponse(
        UUID testId,
        String determinedLevel,
        int score,
        int totalQuestions,
        boolean completed,
        List<QuestionResult> details
) {
    public record QuestionResult(
            int index,
            String question,
            String difficulty,
            String userAnswer,
            String correctAnswer,
            boolean correct,
            String explanation
    ) {}
}
