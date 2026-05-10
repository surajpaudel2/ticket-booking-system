package com.tbs.eventservice.exception;

/** Thrown when a requested fixture does not exist in the database. Maps to HTTP 404. */
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(String message) {
        super(message);
    }
}
