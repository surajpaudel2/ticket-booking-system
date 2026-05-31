package com.tbs.paymentservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static ApiResponse<?> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
