package com.tbs.bookingservice.messaging.payload.outbound;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * publisher: booking-service | consumer: notification-service
 * Carries all context needed to send a booking-confirmation email to the user.
 * recipientEmail, recipientFullName: null — notification-service enriches via userId → user-service.
 * homeTeamName, awayTeamName, stadiumName, fixtureDateTime: null — notification-service enriches via fixtureId → event-service.
 */
public record BookingConfirmedPayload(
        Long bookingId,
        Long userId,           // required for notification-service to look up user contact details
        Long fixtureId,        // required for notification-service to look up fixture details
        String recipientEmail,
        String recipientFullName,
        String homeTeamName,
        String awayTeamName,
        String stadiumName,
        LocalDateTime fixtureDateTime,
        int numberOfTickets,
        double totalAmountPaid,
        LocalDateTime confirmedAt
) {}