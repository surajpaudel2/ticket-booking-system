package com.tbs.bookingservice.client.dto.request;

/** Feign request body sent to event-service to reserve seats for a fixture. */
public record ReserveSeatRequest(Long fixtureId, int requestedSeats, Long userId) {}
