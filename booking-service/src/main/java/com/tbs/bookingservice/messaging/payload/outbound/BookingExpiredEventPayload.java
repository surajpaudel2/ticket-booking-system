package com.tbs.bookingservice.messaging.payload.outbound;

import java.time.LocalDateTime;

/**
 * publisher: scheduler in booking-service | consumer: notification-service
 * Published when a PENDING booking passes its expiresAt without payment completion.
 */
public record BookingExpiredEventPayload(
        Long bookingId,
        Long bookingAttemptId,
        Long userId,
        String recipientEmail,
        String recipientName,
        Long fixtureId,
        String homeTeamName,
        String awayTeamName,
        LocalDateTime currentScheduledStartTime,
        int requestedSeats,
        LocalDateTime expiredAt
) {}
