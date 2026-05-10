package com.tbs.bookingservice.exception;

import org.springframework.http.HttpStatus;

/** Runtime exception for booking domain errors. Carries HTTP status for response mapping. */
public class BookingException extends RuntimeException {

    private final HttpStatus status;

    public BookingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
