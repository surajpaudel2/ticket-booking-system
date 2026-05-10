package com.tbs.bookingservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request body for initiating a booking — seat reservation + payment intent creation. */
public class InitiateBookingRequest {

    @NotNull
    @Schema(description = "ID of the user making the booking", example = "42")
    private Long userId;

    @NotNull
    @Schema(description = "ID of the fixture to book seats for", example = "7")
    private Long fixtureId;

    @Min(1) @Max(10)
    @Schema(description = "Number of seats requested (1–10)", example = "2")
    private int requestedSeats;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getFixtureId() { return fixtureId; }
    public void setFixtureId(Long fixtureId) { this.fixtureId = fixtureId; }

    public int getRequestedSeats() { return requestedSeats; }
    public void setRequestedSeats(int requestedSeats) { this.requestedSeats = requestedSeats; }
}
