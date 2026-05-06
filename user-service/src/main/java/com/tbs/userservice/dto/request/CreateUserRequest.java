package com.tbs.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CreateUserRequest",
        description = "Payload containing the required details to book the ticket"
)
public record CreateUserRequest(

        @Schema(description = "The user's unique email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a well-formed email address")
        String email,

        @Schema(description = "The user's legal full name", example = "John Doe")
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Raw password for the account. Will be encrypted before saving.", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}