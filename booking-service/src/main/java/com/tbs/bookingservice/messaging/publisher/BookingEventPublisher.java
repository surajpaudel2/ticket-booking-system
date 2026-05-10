package com.tbs.bookingservice.messaging.publisher;

import com.tbs.bookingservice.messaging.payload.outbound.BookingExpiredEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingPaymentInitiatedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/** Publishes all outbound booking domain events to RabbitMQ via Spring Cloud Stream StreamBridge. */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventPublisher {

    private static final String SEATS_RELEASE_BINDING    = "seatsRelease-out-0";
    private static final String BOOKING_FAILED_BINDING   = "bookingFailed-out-0";
    private static final String PAYMENT_INITIATED_BINDING = "bookingPaymentInitiated-out-0";
    private static final String BOOKING_EXPIRED_BINDING  = "bookingExpired-out-0";

    private final StreamBridge streamBridge;

    // Publishes seat release event so event-service restores the fixture's available seat count
    public void publishSeatsRelease(SeatsReleaseEventPayload payload) {
        log.debug("Publishing seatsRelease: {}", payload);
        streamBridge.send(SEATS_RELEASE_BINDING, payload);
        log.info("Published seatsRelease for fixtureId={}", payload.fixtureId());
    }

    // Publishes booking failure event so notification-service sends a failure email
    public void publishBookingFailed(BookingFailedEventPayload payload) {
        log.debug("Publishing bookingFailed: {}", payload);
        streamBridge.send(BOOKING_FAILED_BINDING, payload);
        log.info("Published bookingFailed for userId={}, fixtureId={}", payload.userId(), payload.fixtureId());
    }

    // Publishes payment initiated event so notification-service sends a checkout link email
    public void publishPaymentInitiated(BookingPaymentInitiatedEventPayload payload) {
        log.debug("Publishing bookingPaymentInitiated: {}", payload);
        streamBridge.send(PAYMENT_INITIATED_BINDING, payload);
        log.info("Published bookingPaymentInitiated for bookingId={}", payload.bookingId());
    }

    // Publishes booking expired event so notification-service sends an expiry email
    public void publishBookingExpired(BookingExpiredEventPayload payload) {
        log.debug("Publishing bookingExpired: {}", payload);
        streamBridge.send(BOOKING_EXPIRED_BINDING, payload);
        log.info("Published bookingExpired for bookingId={}", payload.bookingId());
    }
}
