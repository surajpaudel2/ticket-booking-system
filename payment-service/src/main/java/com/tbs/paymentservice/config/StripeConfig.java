package com.tbs.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Initialises the Stripe SDK with the secret key on application startup. */
@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe SDK initialised");
    }
}
