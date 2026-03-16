package com.devlingo.web.dto;

import java.util.UUID;

public record LessonSummary(
        UUID id,
        String title,
        String lessonType,
        int orderIndex,
        boolean generated
) {}
