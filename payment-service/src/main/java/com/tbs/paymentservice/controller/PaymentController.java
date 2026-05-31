package com.tbs.paymentservice.controller;

import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.ApiResponse;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;
import com.tbs.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Handles internal payment initiation calls from booking-service via Feign. */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Received initiate payment request for bookingId={}", request.bookingId());
        return ResponseEntity.ok(ApiResponse.success(paymentService.initiatePayment(request)));
    }
}
