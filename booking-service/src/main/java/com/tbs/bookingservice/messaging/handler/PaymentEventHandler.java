package com.tbs.bookingservice.messaging.handler;

import com.tbs.bookingservice.messaging.payload.inbound.PaymentFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentSucceededEventPayload;
import com.tbs.bookingservice.service.PaymentResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentResultService paymentResultService;

    public void handlePaymentSucceeded(PaymentSucceededEventPayload payload) {
        log.info("Received paymentSucceeded bookingId={} userId={} paymentIntentId={} amount={} currency={} succeededAt={}",
                payload.bookingId(), payload.userId(), payload.paymentIntentId(),
                payload.amount(), payload.currency(), payload.succeededAt());
        paymentResultService.confirmBooking(payload);
        log.info("Payment succeeded handling completed bookingId={} paymentIntentId={}",
                payload.bookingId(), payload.paymentIntentId());
    }

    public void handlePaymentFailed(PaymentFailedEventPayload payload) {
        log.info("Received paymentFailed bookingId={} userId={} paymentIntentId={} reason={} failedAt={}",
                payload.bookingId(), payload.userId(), payload.paymentIntentId(),
                payload.failureReason(), payload.failedAt());
        paymentResultService.failBooking(payload);
        log.info("Payment failed handling completed bookingId={} paymentIntentId={}",
                payload.bookingId(), payload.paymentIntentId());
    }
}