package com.tbs.paymentservice.messaging.payload.outbound;

import java.time.LocalDateTime;

/** Published to payment.succeeded exchange when Stripe confirms a successful payment. */
public record PaymentSucceededEventPayload(
        Long bookingId,
        Long userId,
        String paymentIntentId,
        double amount,
        String currency,
        LocalDateTime succeededAt
) {}
