package com.tbs.paymentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Feign request body from booking-service to create a Stripe PaymentIntent. */
public record InitiatePaymentRequest(
        @NotNull Long bookingId,
        @Positive double amount,
        @NotBlank String currency,
        @NotNull Long userId
) {}
