package com.tbs.eventservice.service;

import com.tbs.eventservice.dto.request.CreateFixtureRequest;
import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.FixtureResponse;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;

/** Defines the contract for Fixture creation, seat reservation, and seat release operations. */
public interface FixtureService {

    /**
     * Creates a new Fixture linked to an existing Season.
     *
     * @param request the validated Fixture creation request
     * @return the persisted Fixture as a response DTO
     */
    FixtureResponse createFixture(CreateFixtureRequest request);

    /** Reserves seats for a fixture using Redis cache-aside, distributed lock, and DB double-check. */
    ReserveSeatResponse reserveSeats(ReserveSeatRequest request);

    /** Atomically adds seats back to a fixture's available count in DB and Redis. */
    void releaseSeats(Long fixtureId, int seats);
}
