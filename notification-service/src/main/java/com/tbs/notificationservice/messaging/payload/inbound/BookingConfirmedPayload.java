package com.tbs.notificationservice.messaging.payload.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingConfirmedPayload(
        Long bookingId,
        Long userId,
        Long fixtureId,
        String recipientEmail,
        String recipientFullName,
        String homeTeamName,
        String awayTeamName,
        String stadiumName,
        LocalDateTime fixtureDateTime,
        int numberOfTickets,
        BigDecimal totalAmountPaid,
        LocalDateTime confirmedAt
) {}
