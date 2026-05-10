package com.tbs.bookingservice.exception;

import com.tbs.bookingservice.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/** Maps domain and validation exceptions to structured HTTP error responses. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handles booking domain errors with their explicit HTTP status
    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingException(BookingException ex) {
        log.error("BookingException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiErrorResponse(ex.getMessage(), ex.getStatus().value(), LocalDateTime.now()));
    }

    // Returns 400 with a map of field name → validation message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation failure: {}", ex.getMessage(), ex);
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"
                ));
        return ResponseEntity.badRequest().body(errors);
    }

    // Catch-all for any unhandled exception — never leak internal details
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("An unexpected error occurred", 500, LocalDateTime.now()));
    }
}
