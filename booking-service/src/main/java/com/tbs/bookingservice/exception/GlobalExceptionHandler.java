package com.tbs.bookingservice.exception;

import com.tbs.bookingservice.dto.response.ApiResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Maps all exceptions to a uniform ApiResponse wrapper with appropriate HTTP status codes. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handles domain exceptions thrown by booking-service business logic
    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ApiResponse<?>> handleBookingException(BookingException ex) {
        log.error("BookingException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.failure(ex.getMessage()));
    }

    // Returns per-field validation error details as a joined message string
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation failed [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.failure("Validation failed: " + errors));
    }

    // Safety net in case FeignErrorDecoder is bypassed — normalises 404 from downstream
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiResponse<?>> handleFeignNotFound(FeignException.NotFound ex) {
        log.error("FeignException.NotFound [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Event not found"));
    }

    // Safety net for 409 conflicts not caught by FeignErrorDecoder
    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<ApiResponse<?>> handleFeignConflict(FeignException.Conflict ex) {
        log.error("FeignException.Conflict [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure("Insufficient seats available"));
    }

    // Catch-all for any unhandled exception to prevent stack traces leaking to clients
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unhandled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.failure("An unexpected error occurred"));
    }
}
