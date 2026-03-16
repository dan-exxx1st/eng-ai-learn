package com.devlingo.web.dto;

import java.util.UUID;

public record TestResultResponse(
        UUID testId,
        String determinedLevel,
        int score,
        int totalQuestions,
        boolean completed
) {}
