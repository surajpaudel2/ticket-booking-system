package com.tbs.bookingservice.messaging.payload.outbound;


public record SeatsReleaseEventPayload(
        Long fixtureId,
        int requestedSeats,
        Long bookingId,
        String reason
) {}
