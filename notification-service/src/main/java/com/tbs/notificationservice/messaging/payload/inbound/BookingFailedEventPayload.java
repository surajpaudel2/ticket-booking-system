package com.tbs.notificationservice.messaging.payload.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookingFailedEventPayload(
        Long bookingAttemptId,
        Long userId,
        String recipientEmail,
        String recipientName,
        Long fixtureId,
        int requestedSeats,
        String failureReason,
        Integer availableSeats,
        String homeTeamName,
        String awayTeamName,
        LocalDateTime currentScheduledStartTime
) {}
