package com.tbs.paymentservice.exception;

import com.tbs.paymentservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps all exceptions to uniform ApiResponse wrappers with appropriate HTTP status codes. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handles domain payment exceptions with status code from the exception itself
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentException(PaymentException ex) {
        log.error("PaymentException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.failure(ex.getMessage()));
    }

    // Catch-all to prevent stack traces leaking to callers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unhandled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.failure("An unexpected error occurred"));
    }
}
