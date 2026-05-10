package com.tbs.bookingservice.service;

import com.tbs.bookingservice.dto.cache.UserCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/** Manages Redis cache reads and writes for user data and pending booking state. */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingCacheService {

    private static final String KEY_PREFIX_USER            = "user:";
    private static final String KEY_PREFIX_BOOKING_PENDING = "booking:pending:";
    private static final long   BOOKING_TTL_MINUTES        = 15;

    private final RedisTemplate<String, Object> redisTemplate;

    // Looks up cached user data — returns empty on cache miss without throwing
    public Optional<UserCacheDto> findUserFromCache(Long userId) {
        try {
            Object cached = redisTemplate.opsForValue().get(KEY_PREFIX_USER + userId);
            if (cached instanceof UserCacheDto dto) {
                log.debug("Cache hit for userId={}", userId);
                return Optional.of(dto);
            }
            log.debug("Cache miss for userId={}", userId);
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Redis read failed for userId={}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    // Stores pending booking data with a 15-minute TTL matching the payment window
    public void storeBookingPending(Long bookingId, Object bookingData) {
        String key = KEY_PREFIX_BOOKING_PENDING + bookingId;
        redisTemplate.opsForValue().set(key, bookingData, Duration.ofMinutes(BOOKING_TTL_MINUTES));
        log.debug("Stored pending booking in Redis: key={}", key);
    }

    // Removes the pending booking entry — called on expiry or successful payment
    public void evictBookingPending(Long bookingId) {
        String key = KEY_PREFIX_BOOKING_PENDING + bookingId;
        redisTemplate.delete(key);
        log.debug("Evicted pending booking from Redis: key={}", key);
    }
}
