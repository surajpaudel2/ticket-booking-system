package com.tbs.bookingservice.messaging.consumer;

import com.tbs.bookingservice.messaging.handler.PaymentEventHandler;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.inbound.PaymentSucceededEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentEventHandler paymentEventHandler;

    @Bean
    public Consumer<PaymentSucceededEventPayload> paymentSucceeded() {
        return paymentEventHandler::handlePaymentSucceeded;
    }

    @Bean
    public Consumer<PaymentFailedEventPayload> paymentFailed() {
        return paymentEventHandler::handlePaymentFailed;
    }
}

