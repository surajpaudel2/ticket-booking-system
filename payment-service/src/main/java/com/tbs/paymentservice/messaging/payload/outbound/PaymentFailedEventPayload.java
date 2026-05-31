package com.tbs.paymentservice.messaging.payload.outbound;

import java.time.LocalDateTime;

/** Published to payment.failed exchange when Stripe reports a payment failure. */
public record PaymentFailedEventPayload(
        Long bookingId,
        Long userId,
        String paymentIntentId,
        String failureReason,
        LocalDateTime failedAt
) {}
