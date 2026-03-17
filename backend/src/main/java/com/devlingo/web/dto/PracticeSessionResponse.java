package com.devlingo.web.dto;

import java.util.List;
import java.util.UUID;

public record PracticeSessionResponse(
        UUID id,
        String scenarioType,
        List<PracticeMessageResponse> messages,
        String feedback,
        Integer score,
        boolean completed
) {}
