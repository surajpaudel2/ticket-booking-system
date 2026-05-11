package com.tbs.paymentservice.dto.response;

/** Response returned after a Stripe PaymentIntent is created. */
public record InitiatePaymentResponse(String paymentIntentId, String clientSecret) {}
