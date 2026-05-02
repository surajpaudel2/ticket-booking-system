package com.tbs.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(
        name = "UpdateProfileRequest",
        description = "Payload for updating a user's personal profile. All fields are optional; only provided fields will be updated."
)
public record UpdateProfileRequest(

        @Schema(description = "The user's legal full name", example = "John Doe")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "The user's new email address. Requires uniqueness check.", example = "john.new@example.com")
        @Email(message = "Must be a well-formed email address")
        String email,

        @Schema(description = "The user's date of birth", example = "1995-08-15")
        @Past(message = "Date of birth must be a date in the past")
        LocalDate dateOfBirth,

        @Schema(description = "URL pointing to the user's profile picture", example = "https://s3.bucket.com/images/john.png")
        @Pattern(regexp = "^(https?|ftp)://.*$", message = "Must be a valid URL")
        String profilePictureUrl
) {}