package com.tbs.bookingservice.messaging.payload.outbound;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingConfirmedPayload(
        Long bookingId,
        String recipientEmail,
        String recipientFullName,
        String fixtureHomeTeamName,
        String fixtureAwayTeamName,
        String stadiumName,
        LocalDateTime fixtureDateTime,
        int numberOfTickets,
        BigDecimal totalAmountPaid,
        LocalDateTime confirmedAt
) {}