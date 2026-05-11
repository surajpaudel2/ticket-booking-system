package com.tbs.eventservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Manages distributed Redis locks for fixture seat operations.
 * Prevents race conditions when multiple concurrent booking requests
 * target the same fixture simultaneously.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisLockUtil {

    private static final String LOCK_PREFIX = "lock:fixture:";
    private static final long LOCK_TTL_SECONDS = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    // SET NX EX — returns true if this call set the key (lock acquired), false if key already existed
    public boolean acquireLock(Long fixtureId) {
        String key = LOCK_PREFIX + fixtureId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", Duration.ofSeconds(LOCK_TTL_SECONDS));
        boolean success = Boolean.TRUE.equals(acquired);
        if (success) {
            log.info("Seat lock acquired for fixtureId={}", fixtureId);
        } else {
            log.warn("Seat lock already held for fixtureId={}", fixtureId);
        }
        return success;
    }

    // Deletes the lock key — always called in finally to prevent lock leaks
    public void releaseLock(Long fixtureId) {
        redisTemplate.delete(LOCK_PREFIX + fixtureId);
        log.info("Seat lock released for fixtureId={}", fixtureId);
    }
}
