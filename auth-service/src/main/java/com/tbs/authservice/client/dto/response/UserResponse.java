package com.tbs.authservice.client.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        LocalDate dateOfBirth,
        String profilePictureUrl,
        String role,
        boolean active,
        LocalDateTime createdAt
) {}
