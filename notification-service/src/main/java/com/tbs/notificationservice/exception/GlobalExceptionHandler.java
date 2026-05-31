package com.tbs.notificationservice.exception;

import com.tbs.notificationservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationException(NotificationException ex) {
        log.error("NotificationException: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(ApiResponse.failure("An unexpected error occurred"));
    }
}
