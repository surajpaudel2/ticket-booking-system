package com.tbs.notificationservice.messaging.handler;

import com.tbs.notificationservice.client.EventServiceClient;
import com.tbs.notificationservice.client.UserServiceClient;
import com.tbs.notificationservice.client.dto.response.FixtureResponse;
import com.tbs.notificationservice.client.dto.response.UserResponse;
import com.tbs.notificationservice.client.util.FeignResponseUtils;
import com.tbs.notificationservice.messaging.payload.inbound.BookingAttemptNudgeEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingConfirmedPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingExpiredEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingFailedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingPaymentInitiatedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingReminderEventPayload;
import com.tbs.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final EmailService emailService;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;

    public void handleBookingExpired(BookingExpiredEventPayload payload) {
        log.info("Handling booking-expired event bookingId={}", payload.bookingId());
        try {
            emailService.sendBookingExpiredEmail(payload);
            log.info("Completed booking-expired event bookingId={}", payload.bookingId());
        } catch (Exception e) {
            log.error("Unexpected error handling booking-expired event bookingId={}", payload.bookingId(), e);
        }
    }

    // Nudge is best-effort — silent skip prevents DLQ pollution
    public void handleNudge(BookingAttemptNudgeEventPayload payload) {
        log.info("Handling nudge event bookingAttemptId={}", payload.bookingAttemptId());
        try {
            Optional<UserResponse> user = FeignResponseUtils.unwrap(
                    userServiceClient.getUserById(payload.userId()));
            if (user.isEmpty()) {
                log.warn("Skipping nudge — user not found userId={}", payload.userId());
                return;
            }
            Optional<FixtureResponse> fixture = FeignResponseUtils.unwrap(
                    eventServiceClient.getFixtureById(payload.fixtureId()));
            if (fixture.isEmpty()) {
                log.warn("Skipping nudge — fixture not found fixtureId={}", payload.fixtureId());
                return;
            }
            emailService.sendNudgeEmail(payload, user.get(), fixture.get());
            log.info("Completed nudge event bookingAttemptId={}", payload.bookingAttemptId());
        } catch (Exception e) {
            log.warn("Skipping nudge due to downstream error bookingAttemptId={}", payload.bookingAttemptId(), e);
        }
    }

    public void handleReminder(BookingReminderEventPayload payload) {
        log.info("Handling reminder event bookingId={} minutesRemaining={}", payload.bookingId(), payload.minutesRemaining());
        if (payload.minutesRemaining() == 10) {
            emailService.sendReminder1Email(payload);
        } else if (payload.minutesRemaining() == 5) {
            emailService.sendReminder2Email(payload);
        } else {
            log.warn("Unknown minutesRemaining={} for bookingId={} — skipping", payload.minutesRemaining(), payload.bookingId());
            return;
        }
        log.info("Completed reminder event bookingId={}", payload.bookingId());
    }

    public void handleBookingFailed(BookingFailedEventPayload payload) {
        log.info("Handling booking-failed event bookingAttemptId={}", payload.bookingAttemptId());
        try {
            emailService.sendBookingFailedEmail(payload);
            log.info("Completed booking-failed event bookingAttemptId={}", payload.bookingAttemptId());
        } catch (Exception e) {
            log.error("Unexpected error handling booking-failed event bookingAttemptId={}", payload.bookingAttemptId(), e);
        }
    }

    public void handlePaymentInitiated(BookingPaymentInitiatedEventPayload payload) {
        log.info("Handling payment-initiated event bookingId={}", payload.bookingId());
        try {
            emailService.sendPaymentInitiatedEmail(payload);
            log.info("Completed payment-initiated event bookingId={}", payload.bookingId());
        } catch (Exception e) {
            log.error("Unexpected error handling payment-initiated event bookingId={}", payload.bookingId(), e);
        }
    }

    public void handleBookingConfirmed(BookingConfirmedPayload payload) {
        log.info("Handling booking-confirmed event bookingId={}", payload.bookingId());
        try {
            emailService.sendBookingConfirmedEmail(payload);
            log.info("Completed booking-confirmed event bookingId={}", payload.bookingId());
        } catch (Exception e) {
            log.error("Unexpected error handling booking-confirmed event bookingId={}", payload.bookingId(), e);
        }
    }
}
