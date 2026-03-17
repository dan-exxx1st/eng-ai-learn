package com.devlingo.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VocabularyResponse(
        UUID id,
        String word,
        String translation,
        String context,
        String source,
        boolean learned,
        double easeFactor,
        int intervalDays,
        LocalDateTime nextReviewAt,
        LocalDateTime createdAt
) {}
