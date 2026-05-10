package com.tbs.bookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Uniform error response body returned by GlobalExceptionHandler for all error cases. */
public record ApiErrorResponse(
        String message,
        int status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime timestamp
) {}
