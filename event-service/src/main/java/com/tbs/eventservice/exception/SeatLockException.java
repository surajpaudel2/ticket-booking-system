package com.tbs.eventservice.exception;

/** Thrown when the distributed Redis lock for a fixture cannot be acquired. Maps to HTTP 409. */
public class SeatLockException extends RuntimeException {

    public SeatLockException(String message) {
        super(message);
    }
}
