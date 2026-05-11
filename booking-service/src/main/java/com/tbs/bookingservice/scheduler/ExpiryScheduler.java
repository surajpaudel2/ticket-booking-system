package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.messaging.payload.outbound.BookingExpiredEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingCacheService;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scans PENDING bookings past their expiresAt window. Saves seats.release and
 * booking.expired outbox events. Updates booking status to EXPIRED. Evicts Redis cache.
 * Guard: only processes bookings with status = PENDING to prevent double compensation.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final BookingCacheService bookingCacheService;

    // Scans for PENDING bookings whose 15-minute payment window has elapsed
    @Scheduled(fixedDelay = 30000)
    public void processExpiredBookings() {
        List<Booking> expired = bookingRepository.findAllByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, LocalDateTime.now());
        expired.forEach(this::expireBooking);
    }

    // Guards against double-processing, then saves outbox events, updates status, and evicts cache
    private void expireBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Skipping expiry for bookingId={} — status is {}", booking.getId(), booking.getStatus());
            return;
        }
        outboxService.saveEvent(OutboxEventType.SEATS_RELEASE,
                new SeatsReleaseEventPayload(booking.getFixtureId(), booking.getRequestedSeats(),
                        booking.getId(), "BOOKING_EXPIRED"),
                booking.getId(), null);
        outboxService.saveEvent(OutboxEventType.BOOKING_EXPIRED,
                buildExpiredPayload(booking),
                booking.getId(), booking.getBookingAttemptId());
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
        bookingCacheService.evictBookingPending(booking.getId());
        log.info("Booking expired id={}", booking.getId());
    }

    // Constructs the expiry payload — fixture details fetched by notification-service using fixtureId
    private BookingExpiredEventPayload buildExpiredPayload(Booking booking) {
        return new BookingExpiredEventPayload(
                booking.getId(), booking.getBookingAttemptId(), booking.getUserId(),
                null, null,
                booking.getFixtureId(), null, null, null,
                booking.getRequestedSeats(), LocalDateTime.now());
    }
}
