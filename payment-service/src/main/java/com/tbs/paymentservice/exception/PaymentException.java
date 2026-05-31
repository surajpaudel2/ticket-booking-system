package com.tbs.paymentservice.exception;

import org.springframework.http.HttpStatus;

/** Domain exception for payment-service business rule violations and Stripe errors. */
public class PaymentException extends RuntimeException {

    private final HttpStatus status;

    public PaymentException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
