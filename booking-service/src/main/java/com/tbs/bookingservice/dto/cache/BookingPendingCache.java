package com.tbs.bookingservice.dto.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingPendingCache(
        Long bookingId,
        Long fixtureId,
        Long userId,
        String recipientFullName,
        String recipientEmail,
        int requestedSeats,
        double totalAmount,
        String paymentIntentId,
        String homeTeamName,
        String awayTeamName,
        String stadiumName,
        LocalDateTime fixtureDateTime
) {}
