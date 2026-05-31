package com.tbs.bookingservice.messaging.payload.outbound;

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
        LocalDateTime currentScheduledStartTime,
        String paymentIntentId,
        LocalDateTime expiresAt,
        int minutesRemaining
) {}
