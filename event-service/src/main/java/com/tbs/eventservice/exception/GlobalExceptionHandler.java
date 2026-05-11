package com.tbs.eventservice.exception;

import com.tbs.eventservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps all exceptions to uniform ApiResponse wrappers with appropriate HTTP status codes. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404 — fixture does not exist in DB
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEventNotFound(EventNotFoundException ex) {
        log.error("EventNotFoundException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    // 409 — seat count is below what was requested; includes live available count in message
    @ExceptionHandler(InsufficientSeatsException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientSeats(InsufficientSeatsException ex) {
        log.error("InsufficientSeatsException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        String message = "Insufficient seats. Available: " + ex.getAvailableSeats();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(message));
    }

    // 409 — Redis lock contention; caller should retry
    @ExceptionHandler(SeatLockException.class)
    public ResponseEntity<ApiResponse<?>> handleSeatLock(SeatLockException ex) {
        log.error("SeatLockException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    // 400 — Bean Validation failure on request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation failed [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("Validation failed"));
    }

    // 500 — catch-all to prevent stack traces leaking to callers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unhandled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.failure("An unexpected error occurred"));
    }
}
