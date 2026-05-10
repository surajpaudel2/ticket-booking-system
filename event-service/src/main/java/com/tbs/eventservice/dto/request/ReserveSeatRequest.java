package com.tbs.eventservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request body for the reserve-seats endpoint consumed internally by booking-service via Feign. */
public class ReserveSeatRequest {

    @NotNull
    @Schema(description = "ID of the fixture to reserve seats for", example = "7")
    private Long fixtureId;

    @Min(1) @Max(10)
    @Schema(description = "Number of seats to reserve (1–10)", example = "2")
    private int requestedSeats;

    @NotNull
    @Schema(description = "ID of the user requesting the seats", example = "42")
    private Long userId;

    public Long getFixtureId() { return fixtureId; }
    public void setFixtureId(Long fixtureId) { this.fixtureId = fixtureId; }

    public int getRequestedSeats() { return requestedSeats; }
    public void setRequestedSeats(int requestedSeats) { this.requestedSeats = requestedSeats; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
