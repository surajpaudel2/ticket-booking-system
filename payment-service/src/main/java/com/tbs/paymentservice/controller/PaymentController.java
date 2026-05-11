package com.tbs.paymentservice.controller;

import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.ApiResponse;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;
import com.tbs.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes payment initiation endpoints — consumed internally by booking-service via Feign. */
@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing endpoints — internal use by booking-service")
public class PaymentController {

    private final PaymentService paymentService;

    // Delegates to PaymentService and wraps result in ApiResponse
    @PostMapping("/initiate")
    @Operation(
            summary = "Initiate a payment",
            description = "Creates a Stripe PaymentIntent for a booking and returns the clientSecret " +
                    "for frontend Stripe checkout. Currently uses a stub — Stripe SDK integration is pending."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "PaymentIntent created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                    description = "Stripe PaymentIntent creation failed")
    })
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        InitiatePaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment initiated successfully", response));
    }
}
