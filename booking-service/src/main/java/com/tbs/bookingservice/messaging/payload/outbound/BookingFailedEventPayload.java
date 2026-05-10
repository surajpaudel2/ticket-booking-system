package com.tbs.bookingservice.messaging.payload.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * publisher: booking-service | consumer: notification-service
 * Carries all context needed to send a booking-failure email to the user.
 */
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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime currentScheduledStartTime
) {}
