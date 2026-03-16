package com.devlingo.web.dto;

public record DashboardSummary(
        String languageLevel,
        long completedLessons,
        long totalLessons,
        int completionPercentage,
        int activeProgramsCount
) {}
