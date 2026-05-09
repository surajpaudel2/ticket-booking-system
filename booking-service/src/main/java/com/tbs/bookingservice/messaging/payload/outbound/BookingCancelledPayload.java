package com.tbs.bookingservice.messaging.payload.outbound;

import com.tbs.bookingservice.entity.enums.BookingCancellationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingCancelledPayload(
        Long bookingId,
        String recipientEmail,
        String recipientFullName,
        String fixtureHomeTeamName,
        String fixtureAwayTeamName,
        String stadiumName,
        LocalDateTime fixtureDateTime,
        BookingCancellationType cancellationType,
        String cancellationReason,
        BigDecimal refundAmount,
        LocalDateTime cancelledAt
) {}