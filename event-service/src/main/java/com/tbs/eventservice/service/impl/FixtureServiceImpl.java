package com.tbs.eventservice.service.impl;

import com.tbs.eventservice.dto.request.CreateFixtureRequest;
import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.FixtureResponse;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.entity.Season;
import com.tbs.eventservice.exception.EventNotFoundException;
import com.tbs.eventservice.exception.InsufficientSeatsException;
import com.tbs.eventservice.exception.ResourceNotFoundException;
import com.tbs.eventservice.exception.SeatLockException;
import com.tbs.eventservice.mapper.FixtureMapper;
import com.tbs.eventservice.repository.FixtureRepository;
import com.tbs.eventservice.repository.SeasonRepository;
import com.tbs.eventservice.service.FixtureCacheService;
import com.tbs.eventservice.service.FixtureService;
import com.tbs.eventservice.util.RedisLockUtil;
import com.tbs.eventservice.util.SeatValidatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * Handles fixture creation and seat reservation. Implements Redis cache-aside for fast
 * lookups and distributed locking with DB double-check to prevent race conditions
 * under concurrent booking load.
 */
/*
    * TODO:
    *   - Need to fix Redis implementation like what happens if redis isn't available, how does operation works?
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureServiceImpl implements FixtureService {

    private final FixtureRepository fixtureRepository;
    private final SeasonRepository seasonRepository;
    private final FixtureMapper fixtureMapper;
    private final FixtureCacheService fixtureCacheService;
    private final RedisLockUtil redisLockUtil;
    private final TransactionTemplate transactionTemplate;
    private final SeatValidatorUtil seatValidatorUtil;

    // TODO: Need to handle the edge cases while creating the fixture.
    @Override
    @Transactional
    public FixtureResponse createFixture(CreateFixtureRequest request) {
        log.debug("createFixture seasonId={} homeTeam={}", request.getSeasonId(), request.getHomeTeamName());
        Season season = loadSeasonOrThrow(request.getSeasonId());
        Fixture fixture = fixtureMapper.toEntity(request, season);
        Fixture saved = fixtureRepository.save(fixture);
        log.info("Fixture created id={} seasonName={}", saved.getId(), season.getSeasonName());
        return fixtureMapper.toFixtureResponse(saved);
    }

    // TODO: Fix in redis related edge cases.
    @Override
    public ReserveSeatResponse reserveSeats(ReserveSeatRequest request) {
        log.info("reserveSeats fixtureId={} requestedSeats={}", request.getFixtureId(), request.getRequestedSeats());
        resolveAndCheckSeatsOrThrow(request.getFixtureId(), request.getRequestedSeats());

        /*
         * Acquire distributed lock to fail fast and reduce database pressure.
         * Note: This currently requires Redis to be highly available.
         *
         * TODO: Currently fails the request if Redis is completely down.
         *  Future enhancement: Bypass Redis and rely entirely on DB constraints if Redis is unreachable.
         */
        redisLockUtil.acquireLockWithRetryOrThrow(request.getFixtureId());

        try {
            Fixture updated = verifyAndReduceSeats(request.getFixtureId(), request.getRequestedSeats());
            fixtureCacheService.updateCachedSeatCount(request.getFixtureId(), updated.getAvailableSeats());
            return fixtureMapper.toReserveSeatResponse(updated, request.getRequestedSeats());
        }
        finally {
            try {
                redisLockUtil.releaseLock(request.getFixtureId());
            } catch (Exception e) {
                log.error("Failed to release Redis lock for fixtureId={}",
                        request.getFixtureId(), e);
            }
        }
    }

    @Override
    @Transactional
    public void releaseSeats(Long fixtureId, int seats) {
        Optional<Fixture> fixture = fixtureRepository.findById(fixtureId);
        if (fixture.isEmpty()) {
            log.error("releaseSeats failed — fixtureId={} not found, seats={} could not be released",
                    fixtureId, seats);
            return;
        }
        Fixture f = fixture.get();
        f.setAvailableSeats(f.getAvailableSeats() + seats);
        fixtureRepository.save(f);
        fixtureCacheService.updateCachedSeatCount(fixtureId, f.getAvailableSeats());
        log.info("Seats released fixtureId={} count={} newAvailable={}", fixtureId, seats, f.getAvailableSeats());
    }

    private Season loadSeasonOrThrow(Long seasonId) {
        return seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season", seasonId));
    }

    private void resolveAndCheckSeatsOrThrow(Long fixtureId, int requestedSeats) {
        fixtureCacheService.findFixtureFromCache(fixtureId).ifPresentOrElse(
                fixture -> {
                    seatValidatorUtil.validateAvailability(fixture, requestedSeats);
                    log.debug("Cache hit — seat check passed for fixtureId={}", fixtureId);
                },
                () -> {
                    log.debug("Cache miss for fixtureId={} — fetching from DB", fixtureId);
                    Fixture fixture = fixtureRepository.findById(fixtureId)
                            .orElseThrow(() -> new EventNotFoundException(fixtureId));
                    seatValidatorUtil.validateAvailability(fixture, requestedSeats);
                    fixtureCacheService.storeFixtureInCache(fixtureId, fixture);
                }
        );
    }


    private Fixture verifyAndReduceSeats(Long fixtureId, int requestedSeats) {
        return transactionTemplate.execute(status -> {
            Fixture fixture = fixtureRepository.findByIdWithPessimisticLock(fixtureId)
                    .orElseThrow(() -> new EventNotFoundException(fixtureId));
            seatValidatorUtil.validateAvailability(fixture, requestedSeats);
            fixture.setAvailableSeats(fixture.getAvailableSeats() - requestedSeats);
            return fixtureRepository.save(fixture);
        });
    }
}
