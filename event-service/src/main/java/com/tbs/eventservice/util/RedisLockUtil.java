package com.tbs.eventservice.util;

import com.tbs.eventservice.config.LockProperties;
import com.tbs.eventservice.exception.SeatLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

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
    private final LockProperties lockProperties;

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

    // Retries with exponential backoff + jitter; throws SeatLockException if all attempts fail
    public void acquireLockWithRetryOrThrow(Long fixtureId) {
        long waitMs = lockProperties.getInitialWaitMs();
        for (int attempt = 1; attempt <= lockProperties.getMaxAttempts(); attempt++) {
            if (acquireLock(fixtureId)) {
                log.debug("Lock acquired fixtureId={} attempt={}", fixtureId, attempt);
                return;
            }
            log.warn("Lock busy fixtureId={} attempt={}/{}", fixtureId, attempt, lockProperties.getMaxAttempts());
            if (attempt < lockProperties.getMaxAttempts()) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SeatLockException();
                }
            }
            waitMs = Math.min(waitMs * 2, lockProperties.getMaxWaitMs());
            waitMs += ThreadLocalRandom.current().nextLong(20);
        }
        throw new SeatLockException();
    }
}
