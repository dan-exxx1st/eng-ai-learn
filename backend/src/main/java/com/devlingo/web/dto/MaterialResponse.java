package com.devlingo.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        String originalName,
        String contentType,
        long fileSize,
        String status,
        LocalDateTime createdAt
) {}
