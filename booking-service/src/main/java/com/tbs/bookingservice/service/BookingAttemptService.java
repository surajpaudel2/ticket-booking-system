package com.tbs.bookingservice.service;

import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;

import java.util.concurrent.CompletableFuture;

/** Manages BookingAttempt lifecycle — async saves and state transitions during the booking flow. */
public interface BookingAttemptService {

    /** Asynchronously saves a new ATTEMPTED booking attempt and returns its generated ID. */
    CompletableFuture<Long> saveBookingAttemptAsync(Long userId, Long fixtureId, int requestedSeats);

    /** Updates the seat hint text on an existing attempt. */
    void updateAttemptWithSeatHint(Long attemptId, String seatHint);

    /** Marks an attempt as PROCEEDED and links it to the confirmed booking. */
    void updateAttemptAsProceeded(Long attemptId, Long bookingId);

    /** Marks an attempt as FAILED with the given failure reason. */
    void updateAttemptAsFailed(Long attemptId, BookingAttemptFailureReason reason);

    /** Sets nudgeSentAt to now, preventing duplicate nudge emails. */
    void markNudgeSent(Long attemptId);

    /** Computes a human-readable seat availability hint for the customer. */
    String computeSeatHint(int availableSeatsRemaining, int requestedSeats);
}
