package com.tbs.bookingservice.messaging.payload.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;

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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime currentScheduledStartTime,
        int requestedSeats,
        double totalAmount,
        String paymentIntentId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expiresAt
) {}
