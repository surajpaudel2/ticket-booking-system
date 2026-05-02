package com.tbs.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Single envelope for ALL responses — success and error.
 *
 * Success:   ApiResponse.success("User retrieved", userResponse)
 * Error:     ApiResponse.error("User not found")
 * No data:   ApiResponse.success("User banned successfully")
 *
 * @param <T> the type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)   // omits null fields from JSON output
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    // ── Success with data ──────────────────────────────────────────────

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    // ── Success without data (e.g. ban user, delete) ───────────────────

    public static <Void> ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    // ── Error ──────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    // ── Error with data (e.g. validation errors as a map) ─────────────

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, LocalDateTime.now());
    }
}