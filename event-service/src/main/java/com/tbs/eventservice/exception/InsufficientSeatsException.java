package com.tbs.eventservice.exception;

/** Thrown when a fixture does not have enough available seats. Maps to HTTP 409. */
public class InsufficientSeatsException extends RuntimeException {

    private final int availableSeats;

    public InsufficientSeatsException(String message, int availableSeats) {
        super(message);
        this.availableSeats = availableSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
