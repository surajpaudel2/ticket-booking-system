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

@Component
@Slf4j
@RequiredArgsConstructor
public class FixtureSyncScheduler {

    private final FixtureRepository fixtureRepository;
    private final FixtureCacheService fixtureCacheService;

    @Scheduled(fixedDelay = 60000)
    public void syncFixtureSeatCounts() {
        fixtureRepository
                .findAllByCurrentScheduledStartTimeAfter(LocalDateTime.now())
                .forEach(this::syncSeatCount);
    }

    private void syncSeatCount(Fixture fixture) {
        fixtureCacheService.findFixtureFromCache(fixture.getId())
                .filter(cached -> isMismatch(fixture, cached))
                .ifPresent(cached -> {
                    fixtureCacheService.updateCachedSeatCount(fixture.getId(), fixture.getAvailableSeats());
                    log.info("Cache mismatch corrected fixtureId={} dbSeats={} cachedSeats={}",
                            fixture.getId(), fixture.getAvailableSeats(), cached.getAvailableSeats());
                });
    }

    private boolean isMismatch(Fixture dbFixture, Fixture cachedFixture) {
        return dbFixture.getAvailableSeats() != cachedFixture.getAvailableSeats();
    }
}