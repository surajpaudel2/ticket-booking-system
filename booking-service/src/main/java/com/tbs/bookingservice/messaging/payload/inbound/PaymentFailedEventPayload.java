package com.tbs.bookingservice.messaging.payload.inbound;

import java.time.LocalDateTime;

/**
 * Inbound event payload received from payment-service via the payment.failed exchange.
 * Mirrors payment-service's PaymentFailedEventPayload — must stay in sync if that record changes.
 */
public record PaymentFailedEventPayload(
        Long bookingId,
        Long userId,
        String paymentIntentId,
        String failureReason,
        LocalDateTime failedAt
) {}
