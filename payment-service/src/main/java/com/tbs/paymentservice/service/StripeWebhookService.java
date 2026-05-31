package com.tbs.paymentservice.service;

public interface StripeWebhookService {

    /**
     * Verifies the Stripe signature and processes the webhook event.
     *
     * @return true if the event was accepted and processed, false if the signature was invalid
     */
    boolean processWebhook(String rawPayload, String sigHeader);
}