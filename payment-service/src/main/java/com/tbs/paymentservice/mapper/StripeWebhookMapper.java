package com.tbs.paymentservice.mapper;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StripeWebhookMapper {

    private static final String UNKNOWN_FAILURE = "Unknown failure";

    public Optional<PaymentIntent> extractPaymentIntent(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .filter(obj -> obj instanceof PaymentIntent)
                .map(obj -> (PaymentIntent) obj);
    }

    public String extractFailureReason(PaymentIntent intent) {
        return intent.getLastPaymentError() != null
                ? intent.getLastPaymentError().getMessage()
                : UNKNOWN_FAILURE;
    }
}