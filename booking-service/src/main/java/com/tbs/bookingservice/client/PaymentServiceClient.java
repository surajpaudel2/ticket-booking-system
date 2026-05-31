package com.tbs.bookingservice.client;

import com.tbs.bookingservice.client.dto.request.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.response.InitiatePaymentResponse;
import com.tbs.bookingservice.dto.response.ApiResponse;
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
    ApiResponse<InitiatePaymentResponse> initiatePaymentIntent(@RequestBody InitiatePaymentRequest request);
}
