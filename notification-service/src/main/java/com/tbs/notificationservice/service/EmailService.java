package com.tbs.notificationservice.service;

import com.tbs.notificationservice.client.dto.response.FixtureResponse;
import com.tbs.notificationservice.client.dto.response.UserResponse;
import com.tbs.notificationservice.messaging.payload.inbound.BookingAttemptNudgeEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingConfirmedPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingExpiredEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingFailedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingPaymentInitiatedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingReminderEventPayload;

public interface EmailService {

    void sendBookingExpiredEmail(BookingExpiredEventPayload payload);

    void sendNudgeEmail(BookingAttemptNudgeEventPayload payload,
                        UserResponse user,
                        FixtureResponse fixture);

    void sendReminder1Email(BookingReminderEventPayload payload);

    void sendReminder2Email(BookingReminderEventPayload payload);

    void sendBookingFailedEmail(BookingFailedEventPayload payload);

    void sendPaymentInitiatedEmail(BookingPaymentInitiatedEventPayload payload);

    void sendBookingConfirmedEmail(BookingConfirmedPayload payload);
}
