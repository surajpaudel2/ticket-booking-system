package com.tbs.bookingservice.messaging.publisher;

import com.tbs.bookingservice.messaging.payload.outbound.BookingAttemptNudgeEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingExpiredEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingPaymentInitiatedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingReminderEventPayload;
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

    private static final String SEATS_RELEASE_BINDING      = "seatsRelease-out-0";
    private static final String BOOKING_FAILED_BINDING     = "bookingFailed-out-0";
    private static final String PAYMENT_INITIATED_BINDING  = "bookingPaymentInitiated-out-0";
    private static final String BOOKING_EXPIRED_BINDING    = "bookingExpired-out-0";
    private static final String BOOKING_REMINDER_1_BINDING = "bookingReminder1-out-0";
    private static final String BOOKING_REMINDER_2_BINDING = "bookingReminder2-out-0";
    private static final String ATTEMPT_NUDGE_BINDING      = "bookingAttemptNudge-out-0";

    private final StreamBridge streamBridge;

    // Publishes payment initiated event — direct publish, customer is waiting for checkout
    public void publishPaymentInitiated(BookingPaymentInitiatedEventPayload payload) {
        log.debug("Publishing bookingPaymentInitiated: {}", payload);
        streamBridge.send(PAYMENT_INITIATED_BINDING, payload);
        log.info("Published bookingPaymentInitiated for bookingId={}", payload.bookingId());
    }

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
        log.info("Published bookingFailed for userId={}", payload.userId());
    }

    // Publishes first reminder event at the ~5-minute mark before booking expiry
    public void publishReminder1(BookingReminderEventPayload payload) {
        log.debug("Publishing bookingReminder1: {}", payload);
        streamBridge.send(BOOKING_REMINDER_1_BINDING, payload);
        log.info("Published bookingReminder1 for bookingId={}", payload.bookingId());
    }

    // Publishes second reminder event at the ~2-minute mark before booking expiry
    public void publishReminder2(BookingReminderEventPayload payload) {
        log.debug("Publishing bookingReminder2: {}", payload);
        streamBridge.send(BOOKING_REMINDER_2_BINDING, payload);
        log.info("Published bookingReminder2 for bookingId={}", payload.bookingId());
    }

    // Publishes booking expired event so notification-service sends an expiry email
    public void publishBookingExpired(BookingExpiredEventPayload payload) {
        log.debug("Publishing bookingExpired: {}", payload);
        streamBridge.send(BOOKING_EXPIRED_BINDING, payload);
        log.info("Published bookingExpired for bookingId={}", payload.bookingId());
    }

    // Publishes nudge event for abandoned ATTEMPTED bookings older than 30 minutes
    public void publishAttemptNudge(BookingAttemptNudgeEventPayload payload) {
        log.debug("Publishing bookingAttemptNudge: {}", payload);
        streamBridge.send(ATTEMPT_NUDGE_BINDING, payload);
        log.info("Published bookingAttemptNudge for attemptId={}", payload.bookingAttemptId());
    }
}
