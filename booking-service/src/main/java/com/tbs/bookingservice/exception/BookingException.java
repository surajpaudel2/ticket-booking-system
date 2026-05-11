package com.tbs.bookingservice.exception;

import org.springframework.http.HttpStatus;

/** Domain exception for all booking-service business rule violations and downstream errors. */
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
