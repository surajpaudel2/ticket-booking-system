package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.messaging.payload.outbound.BookingAttemptNudgeEventPayload;
import com.tbs.bookingservice.repository.BookingAttemptRepository;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingAttemptService;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scans abandoned BookingAttempts and saves nudge outbox events.
 * Guards: status must be ATTEMPTED, createdAt must be older than 30 min,
 * nudgeSentAt must be null, seatHint must not be null.
 * Critical guard: must verify no active or completed Booking exists for this attempt
 * to avoid nudging customers mid-payment or post-confirmation.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NudgeScheduler {

    private final BookingAttemptRepository bookingAttemptRepository;
    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final BookingAttemptService bookingAttemptService;

    // Finds ATTEMPTED attempts older than 30 min with no nudge sent and no seatHint exclusion
    @Scheduled(fixedDelay = 300000)
    public void processAbandonedAttempts() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<BookingAttempt> candidates = bookingAttemptRepository
                .findAllByStatusAndNudgeSentAtIsNullAndCreatedAtBefore(
                        BookingAttemptStatus.ATTEMPTED, threshold);
        candidates.forEach(this::processNudgeIfEligible);
    }

    // Validates seatHint and booking status before saving nudge event
    private void processNudgeIfEligible(BookingAttempt attempt) {
        if (attempt.getSeatHint() == null) {
            log.debug("Skipping nudge for attemptId={} — seatHint is null", attempt.getId());
            return;
        }
        if (hasActiveOrConfirmedBooking(attempt)) {
            log.debug("Skipping nudge for attemptId={} — booking in progress or confirmed", attempt.getId());
            return;
        }
        outboxService.saveEvent(OutboxEventType.BOOKING_ATTEMPTED_NUDGE,
                buildNudgePayload(attempt), null, attempt.getId());
        bookingAttemptService.markNudgeSent(attempt.getId());
        log.info("Nudge event saved for attemptId={}", attempt.getId());
    }

    // Returns true if a PENDING or CONFIRMED booking is linked to this attempt
    private boolean hasActiveOrConfirmedBooking(BookingAttempt attempt) {
        if (attempt.getBookingId() == null) return false;
        return bookingRepository.findById(attempt.getBookingId())
                .map(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.CONFIRMED)
                .orElse(false);
    }

    // Builds the nudge payload from attempt data
    private BookingAttemptNudgeEventPayload buildNudgePayload(BookingAttempt attempt) {
        return new BookingAttemptNudgeEventPayload(
                attempt.getId(), attempt.getUserId(), attempt.getFixtureId(),
                attempt.getSeatHint(), attempt.getCreatedAt());
    }
}
