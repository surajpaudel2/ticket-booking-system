package com.tbs.gatewayservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RateLimiterConfig {

    // Public routes — unauthenticated, keyed by IP
    @Bean
    @Qualifier("publicRateLimiter")
    @Primary
    public RedisRateLimiter publicRateLimiter(
            @Value("${rate-limiter.public.replenish-rate}") int replenishRate,
            @Value("${rate-limiter.public.burst-capacity}") int burstCapacity) {
        return new RedisRateLimiter(replenishRate, burstCapacity, 1);
    }

    // Authenticated routes — keyed by userId
    @Bean
    @Qualifier("authenticatedRateLimiter")
    public RedisRateLimiter authenticatedRateLimiter(
            @Value("${rate-limiter.authenticated.replenish-rate}") int replenishRate,
            @Value("${rate-limiter.authenticated.burst-capacity}") int burstCapacity) {
        return new RedisRateLimiter(replenishRate, burstCapacity, 1);
    }

    // Admin routes — lower volume
    @Bean
    @Qualifier("adminRateLimiter")
    public RedisRateLimiter adminRateLimiter(
            @Value("${rate-limiter.admin.replenish-rate}") int replenishRate,
            @Value("${rate-limiter.admin.burst-capacity}") int burstCapacity) {
        return new RedisRateLimiter(replenishRate, burstCapacity, 1);
    }
}