package com.tbs.userservice.dto.response;

import com.tbs.userservice.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response payload containing the user's personal profile details")
public record UserResponse(

        @Schema(description = "Unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "The user's registered email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "The user's legal full name", example = "John Doe")
        String fullName,

        @Schema(description = "The user's date of birth", example = "1995-08-15")
        LocalDate dateOfBirth,

        @Schema(description = "URL pointing to the user's profile picture", example = "https://s3.bucket.com/images/john.png")
        String profilePictureUrl,

        @Schema(description = "The user's authorization role", example = "ROLE_TICKET_HOLDER")
        Role role,

        @Schema(description = "Indicates if the user account is currently active and not banned", example = "true")
        boolean active,

        @Schema(description = "The timestamp when the user registered")
        LocalDateTime createdAt
) {}