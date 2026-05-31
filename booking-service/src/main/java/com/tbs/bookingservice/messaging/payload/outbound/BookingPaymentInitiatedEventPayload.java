package com.tbs.bookingservice.messaging.payload.outbound;

import java.time.LocalDateTime;

/**
 * publisher: booking-service | consumer: notification-service
 * Carries payment intent details so notification-service can send a checkout link email.
 */
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
