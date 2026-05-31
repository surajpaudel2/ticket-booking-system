package com.tbs.notificationservice.config;

import com.tbs.notificationservice.client.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers custom Feign components — error decoder for downstream HTTP error translation. */
@Configuration
public class FeignConfig {

    // Maps non-2xx responses from downstream services into NotificationExceptions
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
