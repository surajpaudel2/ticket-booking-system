package com.tbs.eventservice.exception;

/** Thrown when a fixture does not have enough available seats to fulfil a reservation request. */
public class InsufficientSeatsException extends RuntimeException {

    private final int availableSeats;

    public InsufficientSeatsException(int availableSeats) {
        super("Insufficient seats. Available: " + availableSeats);
        this.availableSeats = availableSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
