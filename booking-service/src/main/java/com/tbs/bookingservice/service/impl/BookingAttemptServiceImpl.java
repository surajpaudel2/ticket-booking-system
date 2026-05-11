package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.repository.BookingAttemptRepository;
import com.tbs.bookingservice.service.BookingAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Manages BookingAttempt persistence. All saves run asynchronously to avoid
 * blocking the main booking flow. Updates run via thenAcceptAsync chains.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingAttemptServiceImpl implements BookingAttemptService {

    private final BookingAttemptRepository bookingAttemptRepository;

    // Saves a new ATTEMPTED attempt in a background thread and returns the generated ID
    @Async("bookingAsyncExecutor")
    @Override
    public CompletableFuture<Long> saveBookingAttemptAsync(Long userId, Long fixtureId, int requestedSeats) {
        BookingAttempt attempt = BookingAttempt.builder()
                .userId(userId)
                .fixtureId(fixtureId)
                .requestedSeats(requestedSeats)
                .status(BookingAttemptStatus.ATTEMPTED)
                .build();
        BookingAttempt saved = bookingAttemptRepository.save(attempt);
        log.info("BookingAttempt saved async id={}", saved.getId());
        return CompletableFuture.completedFuture(saved.getId());
    }

    // Sets the seat hint so customers can see availability context in nudge emails
    @Override
    @Transactional
    public void updateAttemptWithSeatHint(Long attemptId, String seatHint) {
        bookingAttemptRepository.findById(attemptId).ifPresentOrElse(attempt -> {
            attempt.setSeatHint(seatHint);
            bookingAttemptRepository.save(attempt);
            log.info("SeatHint updated for attemptId={}", attemptId);
        }, () -> log.warn("Attempt not found for seatHint update id={}", attemptId));
    }

    // Links the attempt to the confirmed booking and marks it PROCEEDED
    @Override
    @Transactional
    public void updateAttemptAsProceeded(Long attemptId, Long bookingId) {
        bookingAttemptRepository.findById(attemptId).ifPresentOrElse(attempt -> {
            attempt.setStatus(BookingAttemptStatus.PROCEEDED);
            attempt.setBookingId(bookingId);
            bookingAttemptRepository.save(attempt);
        }, () -> log.warn("Attempt not found for proceeded update id={}", attemptId));
    }

    // Records the failure reason on the attempt for audit and notification purposes
    @Override
    @Transactional
    public void updateAttemptAsFailed(Long attemptId, BookingAttemptFailureReason reason) {
        bookingAttemptRepository.findById(attemptId).ifPresentOrElse(attempt -> {
            attempt.setStatus(BookingAttemptStatus.FAILED);
            attempt.setFailureReason(reason);
            bookingAttemptRepository.save(attempt);
            log.info("Attempt marked FAILED id={} reason={}", attemptId, reason);
        }, () -> log.warn("Attempt not found for failure update id={}", attemptId));
    }

    // Stamps nudgeSentAt to prevent the nudge scheduler from sending duplicate emails
    @Override
    @Transactional
    public void markNudgeSent(Long attemptId) {
        bookingAttemptRepository.findById(attemptId).ifPresentOrElse(attempt -> {
            attempt.setNudgeSentAt(LocalDateTime.now());
            bookingAttemptRepository.save(attempt);
        }, () -> log.warn("Attempt not found for nudge mark id={}", attemptId));
    }

    // Returns null if no seats remain, otherwise a human-readable hint for the customer
    @Override
    public String computeSeatHint(int availableSeatsRemaining, int requestedSeats) {
        if (availableSeatsRemaining == 0) return null;
        if (availableSeatsRemaining > requestedSeats) {
            return "Still " + availableSeatsRemaining + " seats available";
        }
        return "Only " + availableSeatsRemaining + " seats left";
    }
}
