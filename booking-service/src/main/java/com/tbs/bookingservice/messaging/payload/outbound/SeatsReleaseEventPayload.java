package com.tbs.bookingservice.messaging.payload.outbound;

/**
 * publisher: booking-service | consumer: event-service
 * Signals that reserved seats should be returned to the fixture's available pool.
 */
public record SeatsReleaseEventPayload(
        Long fixtureId,
        int requestedSeats,
        Long bookingId,
        String reason
) {}
