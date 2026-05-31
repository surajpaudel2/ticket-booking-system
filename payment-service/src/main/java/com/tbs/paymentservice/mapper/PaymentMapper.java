package com.tbs.paymentservice.mapper;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;
import com.tbs.paymentservice.entity.Payment;
import com.tbs.paymentservice.entity.enums.PaymentStatus;
import com.tbs.paymentservice.messaging.payload.outbound.PaymentFailedEventPayload;
import com.tbs.paymentservice.messaging.payload.outbound.PaymentSucceededEventPayload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    public PaymentIntentCreateParams toStripeParams(InitiatePaymentRequest request) {
        return PaymentIntentCreateParams.builder()
                .setAmount((long) (request.amount() * 100))
                .setCurrency(request.currency().toLowerCase())
                .putMetadata("bookingId", String.valueOf(request.bookingId()))
                .putMetadata("userId",    String.valueOf(request.userId()))
                .build();
    }

    public Payment toPendingPayment(InitiatePaymentRequest request, String paymentIntentId) {
        return Payment.builder()
                .bookingId(request.bookingId())
                .userId(request.userId())
                .paymentIntentId(paymentIntentId)
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public InitiatePaymentResponse toInitiatePaymentResponse(PaymentIntent intent) {
        return new InitiatePaymentResponse(intent.getId(), intent.getClientSecret());
    }

    public PaymentSucceededEventPayload toSucceededPayload(Payment payment) {
        return new PaymentSucceededEventPayload(
                payment.getBookingId(),
                payment.getUserId(),
                payment.getPaymentIntentId(),
                payment.getAmount(),
                payment.getCurrency(),
                LocalDateTime.now());
    }

    public PaymentFailedEventPayload toFailedPayload(Payment payment, String failureReason) {
        return new PaymentFailedEventPayload(
                payment.getBookingId(),
                payment.getUserId(),
                payment.getPaymentIntentId(),
                failureReason,
                LocalDateTime.now());
    }
}