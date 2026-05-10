package com.tbs.bookingservice.messaging.payload.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime currentScheduledStartTime,
        int requestedSeats,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expiredAt
) {}
