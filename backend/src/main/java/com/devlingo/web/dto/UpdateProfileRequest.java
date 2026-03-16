package com.devlingo.web.dto;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String specialization
) {}
