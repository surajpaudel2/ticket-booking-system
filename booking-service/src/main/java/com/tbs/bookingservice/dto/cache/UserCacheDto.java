package com.tbs.bookingservice.dto.cache;

/** Cached user data fetched from Redis to avoid cross-service calls during booking flow. */
public record UserCacheDto(Long userId, String email, String name) {}
