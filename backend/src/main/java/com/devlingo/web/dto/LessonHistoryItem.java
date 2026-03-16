package com.devlingo.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonHistoryItem(
        UUID lessonId,
        String lessonTitle,
        String lessonType,
        Integer score,
        LocalDateTime completedAt
) {}
