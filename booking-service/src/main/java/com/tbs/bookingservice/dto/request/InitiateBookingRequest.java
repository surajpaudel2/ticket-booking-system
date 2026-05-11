package com.tbs.bookingservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "ID of the fixture to book", example = "42")
    private Long fixtureId;

    @Min(1)
    @Max(10)
    @Schema(description = "Number of seats to book. Min 1, max 10.", example = "2")
    private int requestedSeats;
}
