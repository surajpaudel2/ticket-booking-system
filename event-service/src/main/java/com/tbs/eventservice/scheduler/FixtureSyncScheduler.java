package com.tbs.eventservice.scheduler;

import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.repository.FixtureRepository;
import com.tbs.eventservice.service.FixtureCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Periodically reconciles available seat counts between Redis and DB.
 * DB is always the source of truth. Any Redis value that drifts from DB is corrected.
 * This handles stale cache from mid-transaction failures or Redis evictions.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FixtureSyncScheduler {

    private final FixtureRepository fixtureRepository;
    private final FixtureCacheService fixtureCacheService;

    // Fetches all upcoming fixtures and corrects any seat count drift in Redis
    @Scheduled(fixedDelay = 60000)
    public void syncFixtureSeatCounts() {
        List<Fixture> activeFixtures = fixtureRepository
                .findAllByCurrentScheduledStartTimeAfter(LocalDateTime.now());
        activeFixtures.forEach(this::syncSeatCount);
    }

    // Compares DB and cached seat counts; updates Redis if they differ
    private void syncSeatCount(Fixture fixture) {
        Optional<Fixture> cached = fixtureCacheService.findFixtureFromCache(fixture.getId());
        if (cached.isEmpty()) return;
        int dbSeats = fixture.getAvailableSeats();
        int cachedSeats = cached.get().getAvailableSeats();
        if (dbSeats != cachedSeats) {
            fixtureCacheService.updateCachedSeatCount(fixture.getId(), dbSeats);
            log.info("Cache mismatch corrected fixtureId={} dbSeats={} cachedSeats={}",
                    fixture.getId(), dbSeats, cachedSeats);
        }
    }
}
