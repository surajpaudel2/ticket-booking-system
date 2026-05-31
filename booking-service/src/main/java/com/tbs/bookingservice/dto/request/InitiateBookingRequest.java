package com.tbs.bookingservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for booking initiation — validated before entering the service layer. */
@Getter
@Setter
@NoArgsConstructor
public class InitiateBookingRequest {

    @NotNull
    @Schema(description = "ID of the user making the booking", example = "1")
    private Long userId;

    @NotNull
    @Email
    @Size(max = 254)
    @Schema(description = "Email of the recipient for notifications, if not provided will be sent to the user default email", example = "john.doe@example.com", nullable = true)
    private String recipientEmail;

    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "Full name of the recipient", example = "John Doe")
    private String recipientFullName;

    @NotNull
    @Positive
    @Schema(description = "ID of the fixture to book", example = "42")
    private Long fixtureId;

    @NotNull
    @Min(1)
    @Max(10)
    @Schema(description = "Number of seats to book. Min 1, max 10.", example = "2")
    private Integer requestedSeats;
}
