package com.tbs.eventservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Provides distributed locking over fixture seat modification using Redis SET NX EX. */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisLockUtil {

    private static final String LOCK_PREFIX       = "lock:fixture:";
    private static final long   LOCK_TTL_SECONDS  = 10;
    private static final String LOCK_VALUE        = "locked";

    private final RedisTemplate<String, Object> redisTemplate;

    // Attempts to acquire the lock atomically — returns false immediately if already held
    public boolean acquireLock(Long fixtureId) {
        String key = LOCK_PREFIX + fixtureId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, LOCK_VALUE, Duration.ofSeconds(LOCK_TTL_SECONDS));
        boolean result = Boolean.TRUE.equals(acquired);
        if (result) {
            log.debug("Lock acquired for fixtureId={}", fixtureId);
        } else {
            log.warn("Lock NOT acquired for fixtureId={}", fixtureId);
        }
        return result;
    }

    // Releases the lock — called in finally block after seat modification is complete
    public void releaseLock(Long fixtureId) {
        redisTemplate.delete(LOCK_PREFIX + fixtureId);
        log.debug("Lock released for fixtureId={}", fixtureId);
    }
}
