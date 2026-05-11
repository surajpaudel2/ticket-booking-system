package com.tbs.eventservice.service;

import com.tbs.eventservice.entity.Fixture;

import java.util.Optional;

/** Cache-aside operations for Fixture data — Redis backed with DB as source of truth. */
public interface FixtureCacheService {

    /** Returns cached Fixture if present; empty if cache miss or Redis error. */
    Optional<Fixture> findFixtureFromCache(Long fixtureId);

    /** Stores Fixture in Redis with a 1-hour TTL. */
    void storeFixtureInCache(Long fixtureId, Fixture fixture);

    /** Updates only the availableSeats field on the cached Fixture without a full reload. */
    void updateCachedSeatCount(Long fixtureId, int availableSeats);
}
