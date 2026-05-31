package com.tbs.notificationservice.messaging.payload.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookingAttemptNudgeEventPayload(
        Long bookingAttemptId,
        Long userId,
        Long fixtureId,
        String seatHint,
        LocalDateTime attemptedAt
) {}
