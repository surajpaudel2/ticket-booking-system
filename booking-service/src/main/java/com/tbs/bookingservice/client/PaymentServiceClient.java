package com.tbs.bookingservice.client;

import com.tbs.bookingservice.client.dto.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.InitiatePaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Payment Service.
 * Used to create a Stripe PaymentIntent during booking initiation.
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    // Calls POST /api/v1/payments/initiate on payment-service
    @PostMapping("/api/v1/payments/initiate")
    InitiatePaymentResponse initiatePayment(@RequestBody InitiatePaymentRequest request);
}
