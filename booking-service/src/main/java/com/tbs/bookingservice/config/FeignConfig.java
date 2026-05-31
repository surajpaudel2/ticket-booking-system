package com.tbs.bookingservice.config;

import com.tbs.bookingservice.client.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers custom Feign components — error decoder for downstream HTTP error translation. */
@Configuration
public class FeignConfig {

    // Handles non-2xx responses — maps 404/409/5xx from downstream into domain BookingExceptions
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

}
