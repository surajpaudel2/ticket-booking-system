package com.tbs.notificationservice.client.dto.response;

public record UserResponse(
        Long userId,
        String email,
        String firstName
) {}
