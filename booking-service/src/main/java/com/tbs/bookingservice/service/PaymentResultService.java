package com.tbs.bookingservice.service;

import com.tbs.bookingservice.messaging.payload.inbound.PaymentFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentSucceededEventPayload;

/** Handles booking state transitions driven by payment events from payment-service. */
public interface PaymentResultService {

    /** Confirms the booking and schedules a confirmation notification. */
    void confirmBooking(PaymentSucceededEventPayload payload);

    /** Marks the booking as failed, releases seats, and schedules a failure notification. */
    void failBooking(PaymentFailedEventPayload payload);
}
