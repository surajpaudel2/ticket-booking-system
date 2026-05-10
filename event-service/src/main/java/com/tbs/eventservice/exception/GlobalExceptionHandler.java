package com.tbs.eventservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/** Maps event-service domain exceptions to structured HTTP error responses. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Uniform error body shared by all handlers. */
    private record ApiErrorResponse(String message, int status, LocalDateTime timestamp) {}

    // Returns 404 when a fixture ID does not exist
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EventNotFoundException ex) {
        log.error("EventNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(ex.getMessage(), 404, LocalDateTime.now()));
    }

    // Returns 409 and includes the current available seat count so callers can surface it to users
    @ExceptionHandler(InsufficientSeatsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientSeats(InsufficientSeatsException ex) {
        log.error("InsufficientSeatsException: {} — available: {}", ex.getMessage(), ex.getAvailableSeats(), ex);
        Map<String, Object> body = Map.of(
                "message", ex.getMessage(),
                "status", 409,
                "availableSeats", ex.getAvailableSeats(),
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Returns 409 when the Redis distributed lock for a fixture could not be acquired
    @ExceptionHandler(SeatLockException.class)
    public ResponseEntity<ApiErrorResponse> handleSeatLock(SeatLockException ex) {
        log.error("SeatLockException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(ex.getMessage(), 409, LocalDateTime.now()));
    }

    // Catch-all for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("An unexpected error occurred", 500, LocalDateTime.now()));
    }
}
