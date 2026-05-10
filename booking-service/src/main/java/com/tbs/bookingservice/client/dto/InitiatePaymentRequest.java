package com.tbs.bookingservice.client.dto;

/** Feign request body sent to payment-service to create a Stripe PaymentIntent. */
public record InitiatePaymentRequest(Long bookingId, double amount, String currency, Long userId) {}
