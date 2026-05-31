package com.tbs.eventservice.exception;

import java.time.LocalDateTime;

public class InvalidSeasonDateRangeException extends RuntimeException {

    public InvalidSeasonDateRangeException(LocalDateTime start, LocalDateTime end) {
        super("expectedStartDateTime must be before expectedEndDateTime — got start=%s end=%s"
                .formatted(start, end));
    }
}
