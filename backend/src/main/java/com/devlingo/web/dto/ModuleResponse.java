package com.devlingo.web.dto;

import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        String description,
        int orderIndex,
        String status,
        List<LessonSummary> lessons
) {}
