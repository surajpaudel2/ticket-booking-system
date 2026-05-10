package com.tbs.eventservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Response returned after seats are successfully reserved for a fixture. */
public record ReserveSeatResponse(
        Long fixtureId,
        String homeTeamName,
        String awayTeamName,
        String stadiumName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime currentScheduledStartTime,
        double pricePerSeat,
        int seatsReserved,
        int availableSeatsRemaining
) {}
