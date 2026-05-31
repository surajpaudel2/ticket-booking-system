package com.tbs.paymentservice.messaging.publisher;

import com.tbs.paymentservice.messaging.payload.outbound.PaymentFailedEventPayload;
import com.tbs.paymentservice.messaging.payload.outbound.PaymentSucceededEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/** Publishes all outbound payment domain events to RabbitMQ via Spring Cloud Stream StreamBridge. */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventPublisher {

    // TODO : Make these variables loosely coupled so that if other part of the code also needs then can be used.
    private static final String PAYMENT_SUCCEEDED_BINDING = "paymentSucceeded-out-0";
    private static final String PAYMENT_FAILED_BINDING    = "paymentFailed-out-0";

    private final StreamBridge streamBridge;

    public void publishPaymentSucceeded(PaymentSucceededEventPayload payload) {
        log.debug("Publishing paymentSucceeded: {}", payload);
        streamBridge.send(PAYMENT_SUCCEEDED_BINDING, payload);
        log.info("Published paymentSucceeded for bookingId={}", payload.bookingId());
    }

    public void publishPaymentFailed(PaymentFailedEventPayload payload) {
        log.debug("Publishing paymentFailed: {}", payload);
        streamBridge.send(PAYMENT_FAILED_BINDING, payload);
        log.info("Published paymentFailed for bookingId={}", payload.bookingId());
    }
}
