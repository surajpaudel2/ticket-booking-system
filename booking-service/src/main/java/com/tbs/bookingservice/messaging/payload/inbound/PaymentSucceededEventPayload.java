package com.tbs.bookingservice.messaging.payload.inbound;

import java.time.LocalDateTime;

/**
 * Inbound event payload received from payment-service via the payment.succeeded exchange.
 * Mirrors payment-service's PaymentSucceededEventPayload — must stay in sync if that record changes.
 */
public record PaymentSucceededEventPayload(
        Long bookingId,
        Long userId,
        String paymentIntentId,
        double amount,
        String currency,
        LocalDateTime succeededAt
) {}
