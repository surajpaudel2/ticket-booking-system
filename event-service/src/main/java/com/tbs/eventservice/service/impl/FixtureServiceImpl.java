package com.tbs.eventservice.service.impl;

import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.exception.EventNotFoundException;
import com.tbs.eventservice.exception.InsufficientSeatsException;
import com.tbs.eventservice.exception.SeatLockException;
import com.tbs.eventservice.repository.FixtureRepository;
import com.tbs.eventservice.service.FixtureCacheService;
import com.tbs.eventservice.service.FixtureService;
import com.tbs.eventservice.util.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * Handles seat reservation for fixtures. Implements Redis cache-aside for fast
 * lookups and distributed locking with DB double-check to prevent race conditions
 * under concurrent booking load.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureServiceImpl implements FixtureService {

    private final FixtureRepository fixtureRepository;
    private final FixtureCacheService fixtureCacheService;
    private final RedisLockUtil redisLockUtil;
    private final TransactionTemplate transactionTemplate;

    // Orchestrates cache-check, Redis lock, DB pessimistic lock, and seat deduction
    @Override
    public ReserveSeatResponse reserveSeats(ReserveSeatRequest request) {
        log.info("reserveSeats fixtureId={} requestedSeats={}", request.getFixtureId(), request.getRequestedSeats());
        resolveFixtureOrThrow(request.getFixtureId());
        checkCachedSeatsOrThrow(request.getFixtureId(), request.getRequestedSeats());
        acquireLockOrThrow(request.getFixtureId());
        try {
            Fixture updated = verifyAndReduceSeats(request.getFixtureId(), request.getRequestedSeats());
            fixtureCacheService.updateCachedSeatCount(request.getFixtureId(), updated.getAvailableSeats());
            return buildReserveSeatResponse(updated, request.getRequestedSeats());
        } finally {
            redisLockUtil.releaseLock(request.getFixtureId());
        }
    }

    // Adds seats back to DB and updates Redis — called by SeatsReleaseListener for compensation
    @Override
    @Transactional
    public void releaseSeats(Long fixtureId, int seats) {
        Optional<Fixture> found = fixtureRepository.findById(fixtureId);
        if (found.isEmpty()) {
            log.error("releaseSeats failed — fixtureId={} not found", fixtureId);
            return;
        }
        Fixture fixture = found.get();
        fixture.setAvailableSeats(fixture.getAvailableSeats() + seats);
        fixtureRepository.save(fixture);
        fixtureCacheService.updateCachedSeatCount(fixtureId, fixture.getAvailableSeats());
        log.info("Seats released fixtureId={} count={} newAvailable={}", fixtureId, seats, fixture.getAvailableSeats());
    }

    // Validates fixture exists; populates cache on DB hit; throws 404 if not found
    private Fixture resolveFixtureOrThrow(Long fixtureId) {
        Optional<Fixture> cached = fixtureCacheService.findFixtureFromCache(fixtureId);
        if (cached.isPresent()) return cached.get();
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EventNotFoundException(fixtureId));
        fixtureCacheService.storeFixtureInCache(fixtureId, fixture);
        return fixture;
    }

    // Fast rejection via cached seat count before acquiring the expensive DB lock
    private void checkCachedSeatsOrThrow(Long fixtureId, int requestedSeats) {
        Optional<Fixture> cached = fixtureCacheService.findFixtureFromCache(fixtureId);
        if (cached.isEmpty()) {
            log.debug("Cache miss on seat check for fixtureId={} — DB will confirm inside lock", fixtureId);
            return;
        }
        int cachedSeats = cached.get().getAvailableSeats();
        if (cachedSeats < requestedSeats) throw new InsufficientSeatsException(cachedSeats);
        log.debug("Cache indicates enough seats for fixtureId={}, proceeding to lock", fixtureId);
    }

    // Throws SeatLockException if another request already holds the Redis lock for this fixture
    private void acquireLockOrThrow(Long fixtureId) {
        if (!redisLockUtil.acquireLock(fixtureId)) throw new SeatLockException();
    }

    // DB double-check inside lock with pessimistic write lock — authoritative seat deduction
    private Fixture verifyAndReduceSeats(Long fixtureId, int requestedSeats) {
        return transactionTemplate.execute(status -> {
            Fixture fixture = fixtureRepository.findByIdWithPessimisticLock(fixtureId)
                    .orElseThrow(() -> new EventNotFoundException(fixtureId));
            if (fixture.getAvailableSeats() < requestedSeats) {
                throw new InsufficientSeatsException(fixture.getAvailableSeats());
            }
            fixture.setAvailableSeats(fixture.getAvailableSeats() - requestedSeats);
            return fixtureRepository.save(fixture);
        });
    }

    // Maps Fixture entity fields to the Feign-compatible response record
    private ReserveSeatResponse buildReserveSeatResponse(Fixture fixture, int seatsReserved) {
        return new ReserveSeatResponse(
                fixture.getId(),
                fixture.getHomeTeamName(),
                fixture.getAwayTeamName(),
                fixture.getStadiumName(),
                fixture.getCurrentScheduledStartTime(),
                fixture.getPricePerSeat(),
                seatsReserved,
                fixture.getAvailableSeats());
    }
}
