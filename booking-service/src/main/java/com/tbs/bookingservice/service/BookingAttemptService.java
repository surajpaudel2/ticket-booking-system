package com.tbs.bookingservice.service;

import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.repository.BookingAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Handles all BookingAttempt persistence.
 * Runs asynchronously to avoid blocking the main booking flow.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingAttemptService {

    private final BookingAttemptRepository bookingAttemptRepository;

    // Persists a new ATTEMPTED record off the main thread; returns the generated id via future
    @Async("bookingAsyncExecutor")
    public CompletableFuture<Long> saveBookingAttemptAsync(Long userId, Long fixtureId, int requestedSeats) {
        BookingAttempt attempt = BookingAttempt.builder()
                .userId(userId)
                .fixtureId(fixtureId)
                .requestedSeats(requestedSeats)
                .status(BookingAttemptStatus.ATTEMPTED)
                .build();
        Long id = bookingAttemptRepository.save(attempt).getId();
        log.info("BookingAttempt saved: id={}, userId={}, fixtureId={}", id, userId, fixtureId);
        return CompletableFuture.completedFuture(id);
    }

    // Updates the seat hint on an attempt after a successful seat reservation
    public void updateAttemptWithSeatHint(Long attemptId, String seatHint) {
        bookingAttemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setSeatHint(seatHint);
            bookingAttemptRepository.save(attempt);
            log.debug("SeatHint updated on attemptId={}: {}", attemptId, seatHint);
        });
    }

    // Marks attempt as PROCEEDED and links the created booking id
    public void updateAttemptAsProceeded(Long attemptId, Long bookingId) {
        bookingAttemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setStatus(BookingAttemptStatus.PROCEEDED);
            attempt.setBookingId(bookingId);
            bookingAttemptRepository.save(attempt);
        });
    }

    // Marks attempt as FAILED with the specific reason code
    public void updateAttemptAsFailed(Long attemptId, BookingAttemptFailureReason reason) {
        bookingAttemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setStatus(BookingAttemptStatus.FAILED);
            attempt.setFailureReason(reason);
            bookingAttemptRepository.save(attempt);
            log.info("BookingAttempt failed: id={}, reason={}", attemptId, reason);
        });
    }

    // Computes a human-readable seat availability hint based on remaining vs requested seats
    public String computeSeatHint(int availableSeatsRemaining, int requestedSeats) {
        if (availableSeatsRemaining > requestedSeats) {
            return "Still " + availableSeatsRemaining + " seats available";
        }
        if (availableSeatsRemaining > 0) {
            return "Only " + availableSeatsRemaining + " seats left";
        }
        return null;
    }
}
