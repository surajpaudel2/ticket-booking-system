package com.tbs.bookingservice.client.dto;

/** Feign response from payment-service containing the Stripe PaymentIntent credentials. */
public record InitiatePaymentResponse(String paymentIntentId, String clientSecret) {}
