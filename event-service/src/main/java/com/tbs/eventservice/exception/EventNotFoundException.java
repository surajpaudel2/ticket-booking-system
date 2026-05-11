package com.tbs.eventservice.exception;

/** Thrown when a requested Fixture does not exist in the database. */
public class EventNotFoundException extends RuntimeException {

    private final Long fixtureId;

    public EventNotFoundException(Long fixtureId) {
        super("Fixture not found with id: " + fixtureId);
        this.fixtureId = fixtureId;
    }

    public Long getFixtureId() {
        return fixtureId;
    }
}
