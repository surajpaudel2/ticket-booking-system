package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.mapper.SchedulerMapper;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingCacheService;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// ExpiryScheduler.java
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final BookingCacheService bookingCacheService;
    private final SchedulerMapper schedulerMapper;

    @Scheduled(fixedDelay = 30000)
    public void processExpiredBookings() {
        bookingRepository
                .findAllByStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now())
                .forEach(this::expireBooking);
    }

    private void expireBooking(Booking booking) {
        try {
            if (booking.getStatus() != BookingStatus.PENDING) {
                log.warn("Skipping expiry for bookingId={} — status is {}", booking.getId(), booking.getStatus());
                return;
            }
            outboxService.saveEvent(OutboxEventType.SEATS_RELEASE,
                    schedulerMapper.toSeatsReleasePayload(booking, "BOOKING_EXPIRED"),
                    booking.getId(), null);
            outboxService.saveEvent(OutboxEventType.BOOKING_EXPIRED,
                    schedulerMapper.toExpiredPayload(booking),
                    booking.getId(), booking.getBookingAttemptId());
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            bookingCacheService.evictBookingPending(booking.getId());
            log.info("Booking expired id={}", booking.getId());
        } catch (Exception e) {
            log.error("Error occurred while expiring bookingId={}", booking.getId(), e);
        }
    }
}
