package com.devlingo.web.dto;

import java.util.List;

public record QuestionDto(
        int index,
        String question,
        List<String> options,
        String difficulty
) {}
