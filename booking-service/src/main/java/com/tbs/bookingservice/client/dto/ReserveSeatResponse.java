package com.tbs.bookingservice.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Feign response from event-service after seats are successfully reserved. */
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
