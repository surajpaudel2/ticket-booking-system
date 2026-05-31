package com.tbs.paymentservice.controller;

import com.tbs.paymentservice.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives Stripe webhook events and delegates all processing to StripeWebhookService.
 * Always returns 200 OK after processing — never propagates exceptions to Stripe,
 * as a non-2xx response would trigger unnecessary retries.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        if (!stripeWebhookService.processWebhook(rawPayload, sigHeader))
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok().build();
    }
}
