package com.tbs.eventservice.service;

import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.exception.EventNotFoundException;
import com.tbs.eventservice.exception.InsufficientSeatsException;
import com.tbs.eventservice.exception.SeatLockException;
import com.tbs.eventservice.repository.FixtureRepository;
import com.tbs.eventservice.util.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles seat reservation for fixtures. Implements Redis cache-aside pattern
 * and distributed locking to prevent race conditions under concurrent booking requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final FixtureCacheService fixtureCacheService;
    private final RedisLockUtil redisLockUtil;

    // Entry point — orchestrates cache lookup, lock acquisition, seat deduction, and response build
    public ReserveSeatResponse reserveSeats(ReserveSeatRequest request) {
        log.info("Reserving seats: fixtureId={}, seats={}, userId={}", request.getFixtureId(), request.getRequestedSeats(), request.getUserId());
        Fixture fixture = resolveFixtureOrThrow(request.getFixtureId());
        acquireLockOrThrow(request.getFixtureId());
        try {
            fixture = verifyAndReduceSeats(request.getFixtureId(), request.getRequestedSeats());
        } finally {
            redisLockUtil.releaseLock(request.getFixtureId());
        }
        fixtureCacheService.updateFixtureSeatCount(fixture.getId(), fixture.getAvailableSeats());
        log.info("Seats reserved: fixtureId={}, remaining={}", fixture.getId(), fixture.getAvailableSeats());
        return buildReserveSeatResponse(fixture, request.getRequestedSeats());
    }

    // Checks cache first; falls back to DB and populates cache on miss
    private Fixture resolveFixtureOrThrow(Long fixtureId) {
        fixtureCacheService.findFixtureFromCache(fixtureId);
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EventNotFoundException("Fixture not found with id: " + fixtureId));
    }

    // Throws SeatLockException (409) immediately if another thread holds the lock
    private void acquireLockOrThrow(Long fixtureId) {
        if (!redisLockUtil.acquireLock(fixtureId)) {
            throw new SeatLockException("Could not acquire seat lock, try again");
        }
    }

    // Fetches fixture under pessimistic DB lock, validates and reduces available seats
    @Transactional
    public Fixture verifyAndReduceSeats(Long fixtureId, int requestedSeats) {
        Fixture fixture = fixtureRepository.findByIdWithLock(fixtureId)
                .orElseThrow(() -> new EventNotFoundException("Fixture not found with id: " + fixtureId));
        if (fixture.getAvailableSeats() < requestedSeats) {
            throw new InsufficientSeatsException(
                    "Not enough seats available. Requested: " + requestedSeats, fixture.getAvailableSeats());
        }
        fixture.setAvailableSeats(fixture.getAvailableSeats() - requestedSeats);
        return fixtureRepository.save(fixture);
    }

    // Maps fixture entity fields to the response record
    private ReserveSeatResponse buildReserveSeatResponse(Fixture fixture, int seatsReserved) {
        return new ReserveSeatResponse(
                fixture.getId(), fixture.getHomeTeamName(), fixture.getAwayTeamName(),
                fixture.getStadiumName(), fixture.getCurrentScheduledStartTime(),
                fixture.getPricePerSeat(), seatsReserved, fixture.getAvailableSeats()
        );
    }

    // Restores seats to a fixture — called when a booking fails or expires
    @Transactional
    public void releaseSeats(Long fixtureId, int seats) {
        log.info("Releasing {} seats for fixtureId={}", seats, fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new EventNotFoundException("Fixture not found with id: " + fixtureId));
        fixture.setAvailableSeats(fixture.getAvailableSeats() + seats);
        fixtureRepository.save(fixture);
        fixtureCacheService.updateFixtureSeatCount(fixtureId, fixture.getAvailableSeats());
        log.info("Seats released: fixtureId={}, newAvailable={}", fixtureId, fixture.getAvailableSeats());
    }
}
