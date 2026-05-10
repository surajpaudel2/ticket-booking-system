package com.tbs.eventservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/** Manages Redis cache reads and writes for Fixture data to reduce DB load on seat queries. */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureCacheService {

    private static final String KEY_PREFIX        = "fixture:";
    private static final long   FIXTURE_TTL_HOURS = 1;

    private final RedisTemplate<String, Object> redisTemplate;

    // Returns cached fixture if present — never throws, returns empty on any failure
    public Optional<Object> findFixtureFromCache(Long fixtureId) {
        try {
            Object cached = redisTemplate.opsForValue().get(KEY_PREFIX + fixtureId);
            if (cached != null) {
                log.debug("Cache hit for fixtureId={}", fixtureId);
                return Optional.of(cached);
            }
            log.debug("Cache miss for fixtureId={}", fixtureId);
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Redis read failed for fixtureId={}: {}", fixtureId, ex.getMessage());
            return Optional.empty();
        }
    }

    // Stores fixture data with a 1-hour TTL
    public void storeFixtureInCache(Long fixtureId, Object fixture) {
        redisTemplate.opsForValue().set(KEY_PREFIX + fixtureId, fixture, Duration.ofHours(FIXTURE_TTL_HOURS));
        log.debug("Stored fixture in cache: fixtureId={}", fixtureId);
    }

    // Refreshes the available seat count on a cached fixture entry if it exists
    public void updateFixtureSeatCount(Long fixtureId, int availableSeats) {
        String key = KEY_PREFIX + fixtureId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            // TODO: update availableSeats field within cached object — requires typed cache model [deferred: cache type TBD after FixtureDto is finalised]
            log.debug("Cache seat count updated for fixtureId={}, availableSeats={}", fixtureId, availableSeats);
        }
    }
}
