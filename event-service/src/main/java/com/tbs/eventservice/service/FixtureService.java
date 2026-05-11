package com.tbs.eventservice.service;

import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;

/** Fixture seat reservation and release operations — cache-aside with distributed locking. */
public interface FixtureService {

    /** Reserves seats for a fixture using Redis cache-aside, distributed lock, and DB double-check. */
    ReserveSeatResponse reserveSeats(ReserveSeatRequest request);

    /** Atomically adds seats back to a fixture's available count in DB and Redis. */
    void releaseSeats(Long fixtureId, int seats);
}
