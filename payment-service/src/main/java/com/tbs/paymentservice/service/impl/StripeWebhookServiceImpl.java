package com.tbs.paymentservice.service.impl;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.tbs.paymentservice.mapper.StripeWebhookMapper;
import com.tbs.paymentservice.service.PaymentService;
import com.tbs.paymentservice.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    private static final String PAYMENT_INTENT_FAILED =  "payment_intent.payment_failed";

    private final PaymentService paymentService;
    private final StripeWebhookMapper stripeWebhookMapper;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    public boolean processWebhook(String rawPayload, String sigHeader) {
        Optional<Event> event = verifyAndConstructEvent(rawPayload, sigHeader);
        if (event.isEmpty()) {
            return false;
        }

        dispatch(event.get());
        return true;
    }

    private Optional<Event> verifyAndConstructEvent(String rawPayload, String sigHeader) {
        try {
            return Optional.of(Webhook.constructEvent(rawPayload, sigHeader, webhookSecret));
        } catch (SignatureVerificationException ex) {
            log.warn("Invalid Stripe webhook signature: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private void dispatch(Event event) {
        log.info("Received Stripe event type={}", event.getType());

        switch (event.getType()) {
            case PAYMENT_INTENT_SUCCEEDED -> handleSucceeded(event);

            case PAYMENT_INTENT_FAILED    -> handleFailed(event);

            default -> log.debug("Unhandled Stripe event type={} — no action taken", event.getType());
        }
    }

    private void handleSucceeded(Event event) {
        stripeWebhookMapper.extractPaymentIntent(event).ifPresent(intent -> {
            log.info("Handling payment_intent.succeeded intentId={}", intent.getId());
            paymentService.handlePaymentSucceeded(intent.getId());
        });
    }

    private void handleFailed(Event event) {
        stripeWebhookMapper.extractPaymentIntent(event).ifPresent(intent -> {
            String reason = stripeWebhookMapper.extractFailureReason(intent);
            log.info("Handling payment_intent.payment_failed intentId={} reason={}", intent.getId(), reason);
            paymentService.handlePaymentFailed(intent.getId(), reason);
        });
    }
}