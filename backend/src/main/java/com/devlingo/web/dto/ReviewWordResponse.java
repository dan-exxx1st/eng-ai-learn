package com.devlingo.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewWordResponse(
    UUID wordId,
    String word,
    double easeFactor,
    int intervalDays,
    LocalDateTime nextReviewAt
) {}
