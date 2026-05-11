package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.service.BookingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Manages Redis operations for booking-service. Stores pending booking window
 * with TTL and provides safe cache reads that never throw.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCacheServiceImpl implements BookingCacheService {

    private static final String KEY_PREFIX_BOOKING_PENDING = "booking:pending:";
    private static final long TTL_MINUTES = 15;

    private final RedisTemplate<String, Object> redisTemplate;

    // Stores booking pending state with 15-minute TTL so expiry scheduler has a fast cache check
    @Override
    public void storeBookingPending(Long bookingId, Object data) {
        String key = KEY_PREFIX_BOOKING_PENDING + bookingId;
        redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(TTL_MINUTES));
        log.info("Stored booking:pending:{} with TTL={}min", bookingId, TTL_MINUTES);
    }

    // Removes the pending key when a booking expires, is confirmed, or is cancelled
    @Override
    public void evictBookingPending(Long bookingId) {
        String key = KEY_PREFIX_BOOKING_PENDING + bookingId;
        redisTemplate.delete(key);
        log.info("Evicted booking:pending:{}", bookingId);
    }

    // Returns true if the booking window is still active in Redis
    @Override
    public boolean isBookingPendingInCache(Long bookingId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX_BOOKING_PENDING + bookingId));
        } catch (Exception ex) {
            log.warn("Redis check failed for bookingId={}: {}", bookingId, ex.getMessage());
            return false;
        }
    }
}
