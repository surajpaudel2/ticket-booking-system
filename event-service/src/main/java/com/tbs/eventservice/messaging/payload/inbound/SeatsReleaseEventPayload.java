package com.tbs.eventservice.messaging.payload.inbound;

/**
 * Inbound event payload. Published by booking-service, consumed by event-service.
 * Triggers seat count restoration in DB and Redis when a booking fails or expires.
 */
public record SeatsReleaseEventPayload(
        Long fixtureId,
        int requestedSeats,
        Long bookingId,
        String reason
) {}
