package com.tbs.bookingservice.client;

import com.tbs.bookingservice.exception.BookingException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

/** Translates Feign HTTP error responses from downstream services into BookingExceptions. */
public class FeignErrorDecoder implements ErrorDecoder {

    // Maps downstream HTTP status codes to domain exceptions with appropriate HTTP status
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new BookingException("Event not found", HttpStatus.NOT_FOUND);
            case 409 -> new BookingException("Insufficient seats available", HttpStatus.CONFLICT);
            default -> new BookingException("Downstream service error", HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
