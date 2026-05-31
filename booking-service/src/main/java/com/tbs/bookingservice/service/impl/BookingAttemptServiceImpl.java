package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.mapper.BookingMapper;
import com.tbs.bookingservice.repository.BookingAttemptRepository;
import com.tbs.bookingservice.service.BookingAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Manages BookingAttempt persistence. All saves run asynchronously to avoid
 * blocking the main booking flow. Updates run via thenAcceptAsync chains.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingAttemptServiceImpl implements BookingAttemptService {

    private final BookingAttemptRepository bookingAttemptRepository;
    private final BookingMapper bookingMapper;

    @Async("bookingAsyncExecutor")
    @Override
    public CompletableFuture<Long> saveBookingAttemptAsync(Long userId, Long fixtureId, int requestedSeats) {
        BookingAttempt saved = bookingAttemptRepository.save(
                bookingMapper.toNewBookingAttempt(userId, fixtureId, requestedSeats));
        log.info("BookingAttempt saved async id={}", saved.getId());
        return CompletableFuture.completedFuture(saved.getId());
    }

    @Override
    @Transactional
    public void updateAttemptWithSeatHint(Long attemptId, String seatHint) {
        findAndUpdate(attemptId, "seatHint", attempt -> {
            attempt.setSeatHint(seatHint);
            log.info("SeatHint updated for attemptId={}", attemptId);
        });
    }

    @Override
    @Transactional
    public void updateAttemptAsProceeded(Long attemptId, Long bookingId) {
        findAndUpdate(attemptId, "proceeded", attempt -> {
            attempt.setStatus(BookingAttemptStatus.PROCEEDED);
            attempt.setBookingId(bookingId);
        });

    }

    @Override
    @Transactional
    public void updateAttemptAsFailed(Long attemptId, BookingAttemptFailureReason reason) {
        findAndUpdate(attemptId, "failure", attempt -> {
            attempt.setStatus(BookingAttemptStatus.FAILED);
            attempt.setFailureReason(reason);
            log.info("Attempt marked FAILED id={} reason={}", attemptId, reason);
        });
    }

    @Override
    @Transactional
    public void markNudgeSent(Long attemptId) {
        findAndUpdate(attemptId, "nudge", attempt ->
                attempt.setNudgeSentAt(LocalDateTime.now()));
    }

    @Override
    public String computeSeatHint(int availableSeatsRemaining, int requestedSeats) {
        if (availableSeatsRemaining == 0)                        return null;
        if (availableSeatsRemaining > requestedSeats)            return "Still " + availableSeatsRemaining + " seats available";
        return "Only " + availableSeatsRemaining + " seats left";
    }

    // Finds attempt by id, runs the update, saves — logs warn if not found
    private void findAndUpdate(Long attemptId, String context, Consumer<BookingAttempt> updater) {
        bookingAttemptRepository.findById(attemptId).ifPresentOrElse(attempt -> {
            updater.accept(attempt);
            bookingAttemptRepository.save(attempt);
        }, () -> log.warn("Attempt not found for {} update id={}", context, attemptId));
    }
}