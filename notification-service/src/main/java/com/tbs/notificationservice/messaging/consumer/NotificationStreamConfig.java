package com.tbs.notificationservice.messaging.consumer;

import com.tbs.notificationservice.messaging.handler.NotificationEventHandler;
import com.tbs.notificationservice.messaging.payload.inbound.BookingAttemptNudgeEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingConfirmedPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingExpiredEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingFailedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingPaymentInitiatedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingReminderEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class NotificationStreamConfig {

    private final NotificationEventHandler handler;

    @Bean
    public Consumer<BookingExpiredEventPayload> bookingExpired() {
        return handler::handleBookingExpired;
    }

    @Bean
    public Consumer<BookingAttemptNudgeEventPayload> bookingAttemptNudge() {
        return handler::handleNudge;
    }

    @Bean
    public Consumer<BookingReminderEventPayload> bookingReminder() {
        return handler::handleReminder;
    }

    @Bean
    public Consumer<BookingFailedEventPayload> bookingFailed() {
        return handler::handleBookingFailed;
    }

    @Bean
    public Consumer<BookingPaymentInitiatedEventPayload> paymentInitiated() {
        return handler::handlePaymentInitiated;
    }

    @Bean
    public Consumer<BookingConfirmedPayload> bookingConfirmed() {
        return handler::handleBookingConfirmed;
    }
}
