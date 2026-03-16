package com.devlingo.web.dto;

import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        String lessonType,
        String content,
        boolean generated
) {}
