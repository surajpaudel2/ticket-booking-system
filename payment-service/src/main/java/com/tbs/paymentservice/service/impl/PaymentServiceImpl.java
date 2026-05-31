package com.tbs.paymentservice.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;
import com.tbs.paymentservice.entity.Payment;
import com.tbs.paymentservice.entity.enums.OutboxEventType;
import com.tbs.paymentservice.entity.enums.PaymentStatus;
import com.tbs.paymentservice.exception.PaymentException;
import com.tbs.paymentservice.mapper.PaymentMapper;
import com.tbs.paymentservice.repository.PaymentRepository;
import com.tbs.paymentservice.service.OutboxService;
import com.tbs.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        PaymentIntent intent = createStripePaymentIntent(request);

        // Saved payment as pending because payment is just being initiated, don't know whether it is success or failed yet.
        Payment payment = paymentRepository.save(paymentMapper.toPendingPayment(request, intent.getId()));
        log.info("Payment PENDING created id={} intentId={}", payment.getId(), intent.getId());
        return paymentMapper.toInitiatePaymentResponse(intent);
    }


    @Override
    @Transactional
    public void handlePaymentSucceeded(String paymentIntentId) {
        Payment payment = resolvePaymentOrThrow(paymentIntentId);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Payment already SUCCEEDED for intentId={} — skipping", paymentIntentId);
            return;
        }
        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
        outboxService.saveEvent(OutboxEventType.PAYMENT_SUCCEEDED,
                paymentMapper.toSucceededPayload(payment), payment.getId());
        log.info("Payment SUCCEEDED for id={} intentId={}", payment.getId(), paymentIntentId);
    }

    @Override
    @Transactional
    public void handlePaymentFailed(String paymentIntentId, String failureReason) {
        Payment payment = resolvePaymentOrThrow(paymentIntentId);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.warn("Payment already FAILED for intentId={} — skipping", paymentIntentId);
            return;
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        outboxService.saveEvent(OutboxEventType.PAYMENT_FAILED,
                paymentMapper.toFailedPayload(payment, failureReason), payment.getId());
        log.info("Payment FAILED for id={} intentId={} reason={}", payment.getId(), paymentIntentId, failureReason);
    }

    private PaymentIntent createStripePaymentIntent(InitiatePaymentRequest request) {
        try {
            return PaymentIntent.create(paymentMapper.toStripeParams(request));
        } catch (StripeException ex) {
            log.error("Stripe PaymentIntent creation failed for bookingId={}: {}", request.bookingId(), ex.getMessage());
            throw new PaymentException("Failed to create payment intent", HttpStatus.BAD_GATEWAY);
        }
    }

    private Payment resolvePaymentOrThrow(String paymentIntentId) {
        return paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for intentId=" + paymentIntentId, HttpStatus.NOT_FOUND));
    }
}