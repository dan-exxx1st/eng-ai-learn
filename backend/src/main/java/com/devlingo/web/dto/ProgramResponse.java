package com.devlingo.web.dto;

import java.util.List;
import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String title,
        String description,
        String languageLevel,
        String specialization,
        String status,
        List<ModuleResponse> modules
) {}
