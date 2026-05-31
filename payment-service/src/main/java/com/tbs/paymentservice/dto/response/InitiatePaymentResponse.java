package com.tbs.paymentservice.dto.response;

/** Response returned to booking-service containing Stripe PaymentIntent credentials. */
public record InitiatePaymentResponse(String paymentIntentId, String clientSecret) {}
