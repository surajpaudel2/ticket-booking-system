package com.tbs.paymentservice.exception;

import com.tbs.paymentservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Maps all exceptions to a uniform ApiResponse with appropriate HTTP status codes. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentException(PaymentException ex) {
        log.error("PaymentException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage());
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.failure("Validation failed: " + errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unhandled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.failure("An unexpected error occurred"));
    }
}
