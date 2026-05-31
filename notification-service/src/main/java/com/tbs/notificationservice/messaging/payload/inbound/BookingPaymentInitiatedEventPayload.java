package com.tbs.notificationservice.messaging.payload.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookingPaymentInitiatedEventPayload(
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
        double totalAmount,
        String paymentIntentId,
        LocalDateTime expiresAt
) {}
