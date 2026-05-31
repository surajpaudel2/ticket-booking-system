package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.mapper.SchedulerMapper;
import com.tbs.bookingservice.repository.BookingAttemptRepository;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingAttemptService;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scans abandoned BookingAttempts and saves nudge outbox events.
 * Guards: status must be ATTEMPTED, createdAt must be older than 30 min,
 * nudgeSentAt must be null, seatHint must not be null.
 * Critical guard: must verify no active or completed Booking exists for this attempt
 * to avoid nudging customers mid-payment or post-confirmation.
 *
 * TODO: Revisit before enabling nudge emails.
 *  - Confirm what the original nudge email content/template was meant to be
 *  - Decide whether to call event-service first to verify seat availability
 *    before sending the nudge (avoid nudging for unavailable seats)
 *  - Only enable this scheduler once both of the above are resolved
 */
// NudgeScheduler.java
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingNudgeScheduler {

    private final BookingAttemptRepository bookingAttemptRepository;
    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final BookingAttemptService bookingAttemptService;
    private final SchedulerMapper schedulerMapper;

    @Scheduled(fixedDelay = 300000)
    public void processAbandonedAttempts() {
        bookingAttemptRepository
                .findAllByStatusAndNudgeSentAtIsNullAndCreatedAtBefore(
                        BookingAttemptStatus.ATTEMPTED, LocalDateTime.now().minusMinutes(30))
                .forEach(this::processNudgeIfEligible);
    }

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
                schedulerMapper.toNudgePayload(attempt), null, attempt.getId());
        bookingAttemptService.markNudgeSent(attempt.getId());
        log.info("Nudge event saved for attemptId={}", attempt.getId());
    }

    private boolean hasActiveOrConfirmedBooking(BookingAttempt attempt) {
        if (attempt.getBookingId() == null) return false;
        return bookingRepository.findById(attempt.getBookingId())
                .map(b -> b.getStatus() == BookingStatus.PENDING
                        || b.getStatus() == BookingStatus.CONFIRMED)
                .orElse(false);
    }
}