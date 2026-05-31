package com.tbs.notificationservice.messaging.payload.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

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
