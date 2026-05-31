package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.exception.BookingException;
import com.tbs.bookingservice.mapper.BookingMapper;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentSucceededEventPayload;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingCacheService;
import com.tbs.bookingservice.service.OutboxService;
import com.tbs.bookingservice.service.PaymentResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentResultServiceImpl implements PaymentResultService {

    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final BookingCacheService bookingCacheService;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public void confirmBooking(PaymentSucceededEventPayload payload) {
        Booking booking = resolveBookingOrThrow(payload.bookingId());
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.warn("Booking already CONFIRMED for bookingId={} — skipping duplicate event", payload.bookingId());
            return;
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        bookingRepository.save(booking);

        // TODO :
        //  - We are solely relying on cache only, don't rely only in cache.
        outboxService.saveEvent(
                OutboxEventType.BOOKING_CONFIRMED,
                bookingMapper.toConfirmedPayload(bookingCacheService.getBookingPending(booking.getId()).get(), booking),
                booking.getId(),
                booking.getBookingAttemptId()
        );

        bookingCacheService.evictBookingPending(booking.getId());
        log.info("Booking CONFIRMED for bookingId={} paymentIntentId={}", booking.getId(), payload.paymentIntentId());
    }

    @Override
    @Transactional
    public void failBooking(PaymentFailedEventPayload payload) {
        Booking booking = resolveBookingOrThrow(payload.bookingId());
        if (isAlreadyTerminated(booking)) {
            log.warn("Booking already {} for bookingId={} — skipping duplicate event", booking.getStatus(), payload.bookingId());
            return;
        }

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        bookingCacheService.evictBookingPending(booking.getId());

        outboxService.saveEvent(
                OutboxEventType.SEATS_RELEASE,
                bookingMapper.toSeatsReleasePayload(booking, "PAYMENT_FAILED"),
                booking.getId(),
                null
        );

        outboxService.saveEvent(
                OutboxEventType.BOOKING_FAILED,
                bookingMapper.toBookingFailedPayloadForBooking(booking, null, payload.failureReason()),
                booking.getId(),
                null
        );

        log.info("Booking FAILED for bookingId={} reason={}", booking.getId(), payload.failureReason());
    }

    private Booking resolveBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(
                        "Booking not found for bookingId=" + bookingId, HttpStatus.NOT_FOUND));
    }

    private boolean isAlreadyTerminated(Booking booking) {
        return booking.getStatus() == BookingStatus.FAILED
                || booking.getStatus() == BookingStatus.EXPIRED;
    }

}
