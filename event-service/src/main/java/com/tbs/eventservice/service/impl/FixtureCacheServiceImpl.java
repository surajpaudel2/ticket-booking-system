package com.tbs.eventservice.service.impl;

import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.service.FixtureCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache-aside service for Fixture data. Redis is used for fast event existence
 * checks and available seat count reads. DB is always the source of truth for
 * seat count decisions inside the distributed lock.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureCacheServiceImpl implements FixtureCacheService {

    private static final String KEY_PREFIX = "fixture:";
    private static final long TTL_HOURS = 1;

    private final RedisTemplate<String, Object> redisTemplate;

    // Returns fixture from Redis; swallows all Redis exceptions to avoid cascading failures
    @Override
    public Optional<Fixture> findFixtureFromCache(Long fixtureId) {
        try {
            Object cached = redisTemplate.opsForValue().get(KEY_PREFIX + fixtureId);
            if (cached == null) {
                log.debug("Cache miss for fixtureId={}", fixtureId);
                return Optional.empty();
            }
            log.debug("Cache hit for fixtureId={}", fixtureId);
            return Optional.of((Fixture) cached);
        } catch (Exception ex) {
            log.warn("Redis read failed for fixtureId={}: {}", fixtureId, ex.getMessage());
            return Optional.empty();
        }
    }

    // Stores fixture with TTL — called after a DB fetch to populate the cache
    @Override
    public void storeFixtureInCache(Long fixtureId, Fixture fixture) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + fixtureId, fixture, Duration.ofHours(TTL_HOURS));
            log.info("Fixture stored in cache fixtureId={}", fixtureId);
        } catch (Exception ex) {
            log.warn("Redis write failed for fixtureId={}: {}", fixtureId, ex.getMessage());
        }
    }

    // Updates only availableSeats on the cached fixture to keep Redis consistent with DB
    @Override
    public void updateCachedSeatCount(Long fixtureId, int availableSeats) {
        Optional<Fixture> cached = findFixtureFromCache(fixtureId);
        if (cached.isEmpty()) {
            log.debug("Cache miss on seat count update for fixtureId={} — skipping", fixtureId);
            return;
        }
        cached.get().setAvailableSeats(availableSeats);
        storeFixtureInCache(fixtureId, cached.get());
        log.info("Cache seat count updated fixtureId={} availableSeats={}", fixtureId, availableSeats);
    }
}
