package com.tbs.bookingservice.config;

import com.tbs.bookingservice.client.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers custom Feign components — error decoder for downstream HTTP error translation. */
@Configuration
public class FeignConfig {

    // Registers FeignErrorDecoder globally for all Feign clients in this service
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
