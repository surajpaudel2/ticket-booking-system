package com.tbs.bookingservice.messaging.publisher;

import com.tbs.bookingservice.messaging.payload.outbound.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventPublisher {

    private static final String SEATS_RELEASE_BINDING      = "seatsRelease-out-0";
    private static final String BOOKING_CONFIRMED_BINDING = "bookingConfirmed-out-0";
    private static final String BOOKING_FAILED_BINDING     = "bookingFailed-out-0";
    private static final String PAYMENT_INITIATED_BINDING  = "bookingPaymentInitiated-out-0";
    private static final String BOOKING_EXPIRED_BINDING    = "bookingExpired-out-0";
    private static final String BOOKING_REMINDER_1_BINDING = "bookingReminder1-out-0";
    private static final String BOOKING_REMINDER_2_BINDING = "bookingReminder2-out-0";
    private static final String ATTEMPT_NUDGE_BINDING      = "bookingAttemptNudge-out-0";

    private final StreamBridge streamBridge;

    public void publishPaymentInitiatedForBooking(BookingPaymentInitiatedEventPayload payload) {
        log.debug("Publishing bookingPaymentInitiated: {}", payload);
        streamBridge.send(PAYMENT_INITIATED_BINDING, payload);
        log.info("Published bookingPaymentInitiated for bookingId={}", payload.bookingId());
    }

    public void publishSeatsRelease(SeatsReleaseEventPayload payload) {
        log.debug("Publishing seatsRelease: {}", payload);
        streamBridge.send(SEATS_RELEASE_BINDING, payload);
        log.info("Published seatsRelease for fixtureId={}", payload.fixtureId());
    }

    public void publishBookingFailed(BookingFailedEventPayload payload) {
        log.debug("Publishing bookingFailed: {}", payload);
        streamBridge.send(BOOKING_FAILED_BINDING, payload);
        log.info("Published bookingFailed for userId={}", payload.userId());
    }

    public void publishBookingReminder1(BookingReminderEventPayload payload) {
        log.debug("Publishing bookingReminder1: {}", payload);
        streamBridge.send(BOOKING_REMINDER_1_BINDING, payload);
        log.info("Published bookingReminder1 for bookingId={}", payload.bookingId());
    }

    public void publishBookingReminder2(BookingReminderEventPayload payload) {
        log.debug("Publishing bookingReminder2: {}", payload);
        streamBridge.send(BOOKING_REMINDER_2_BINDING, payload);
        log.info("Published bookingReminder2 for bookingId={}", payload.bookingId());
    }

    public void publishBookingConfirmed(BookingConfirmedPayload payload) {
        log.debug("Publishing bookingConfirmed: {}", payload);
        streamBridge.send(BOOKING_CONFIRMED_BINDING, payload);
        log.info("Published bookingConfirmed for bookingId={}", payload.bookingId());
    }

    public void publishBookingExpired(BookingExpiredEventPayload payload) {
        log.debug("Publishing bookingExpired: {}", payload);
        streamBridge.send(BOOKING_EXPIRED_BINDING, payload);
        log.info("Published bookingExpired for bookingId={}", payload.bookingId());
    }

    public void publishBookingAttemptNudge(BookingAttemptNudgeEventPayload payload) {
        log.debug("Publishing bookingAttemptNudge: {}", payload);
        streamBridge.send(ATTEMPT_NUDGE_BINDING, payload);
        log.info("Published bookingAttemptNudge for attemptId={}", payload.bookingAttemptId());
    }
}
