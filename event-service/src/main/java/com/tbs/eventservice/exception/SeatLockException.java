package com.tbs.eventservice.exception;

/** Thrown when the distributed Redis seat lock cannot be acquired for a fixture. */
public class SeatLockException extends RuntimeException {

    public SeatLockException() {
        super("Could not acquire seat lock. Please try again.");
    }
}
