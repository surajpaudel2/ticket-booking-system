package com.tbs.bookingservice.client.dto.response;

public record InitiatePaymentResponse(String paymentIntentId, String clientSecret) {}
