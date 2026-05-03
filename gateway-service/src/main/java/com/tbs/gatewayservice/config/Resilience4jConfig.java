package com.tbs.gatewayservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.tbs.gatewayservice.constant.GatewayConstants.*;

@Configuration
public class Resilience4jConfig {

    @Bean
    @SuppressWarnings("rawtypes")
    public ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory() {
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory();
        factory.configure(b -> b.circuitBreakerConfig(createCircuitBreakerConfig(10, 50.0f, 10)).build(), CB_AUTH_SERVICE);
        factory.configure(b -> b.circuitBreakerConfig(createCircuitBreakerConfig(10, 50.0f, 10)).build(), CB_USER_SERVICE);
        factory.configure(b -> b.circuitBreakerConfig(createCircuitBreakerConfig(20, 60.0f, 15)).build(), CB_BOOKING_SERVICE);
        factory.configure(b -> b.circuitBreakerConfig(createCircuitBreakerConfig(10, 30.0f, 30)).build(), CB_PAYMENT_SERVICE);
        factory.configure(b -> b.circuitBreakerConfig(createCircuitBreakerConfig(10, 50.0f, 10)).build(), CB_ADMIN_SERVICE);
        return factory;
    }

    private CircuitBreakerConfig createCircuitBreakerConfig(int slidingWindowSize, float failureRateThreshold, int waitDurationSeconds) {
        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
    }
}
