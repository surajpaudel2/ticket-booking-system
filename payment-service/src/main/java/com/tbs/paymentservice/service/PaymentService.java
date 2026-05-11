package com.tbs.paymentservice.service;

import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles Stripe PaymentIntent creation. Currently uses a stub implementation.
 * TODO: Inject Stripe SDK and replace stub with real PaymentIntent API call
 * once Stripe secret key is configured via config server.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    // Logs initiation context, delegates to PaymentIntent creation, and returns credentials
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for bookingId={} amount={} {}",
                request.getBookingId(), request.getAmount(), request.getCurrency());
        InitiatePaymentResponse response = createPaymentIntent(request);
        log.info("PaymentIntent created paymentIntentId={} for bookingId={}",
                response.paymentIntentId(), request.getBookingId());
        return response;
    }

    // TODO: Replace stub with real Stripe SDK call:
    // PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
    //   .setAmount(Math.round(request.getAmount() * 100))
    //   .setCurrency(request.getCurrency())
    //   .putMetadata("bookingId", String.valueOf(request.getBookingId()))
    //   .build());
    // return new InitiatePaymentResponse(intent.getId(), intent.getClientSecret());
    private InitiatePaymentResponse createPaymentIntent(InitiatePaymentRequest request) {
        return new InitiatePaymentResponse(
                "pi_stub_" + request.getBookingId(),
                "secret_stub_" + request.getBookingId());
    }
}
