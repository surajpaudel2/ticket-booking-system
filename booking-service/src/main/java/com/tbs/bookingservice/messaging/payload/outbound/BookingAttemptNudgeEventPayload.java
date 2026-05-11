package com.tbs.bookingservice.messaging.payload.outbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * publisher: booking-service | consumer: notification-service
 * Published when an ATTEMPTED BookingAttempt is abandoned for more than 30 minutes.
 */
public record BookingAttemptNudgeEventPayload(
        Long bookingAttemptId,
        Long userId,
        Long fixtureId,
        String seatHint,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime attemptedAt
) {}
