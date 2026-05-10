package com.tbs.bookingservice.messaging.payload.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * publisher: scheduler in booking-service | consumer: notification-service
 * Sent at intervals to remind users of a pending payment before the booking expires.
 */
public record BookingReminderEventPayload(
        Long bookingId,
        Long userId,
        String recipientEmail,
        String recipientName,
        Long fixtureId,
        String homeTeamName,
        String awayTeamName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime currentScheduledStartTime,
        String paymentIntentId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expiresAt,
        int minutesRemaining
) {}
