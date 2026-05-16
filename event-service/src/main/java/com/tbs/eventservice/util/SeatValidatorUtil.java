package com.tbs.eventservice.util;

import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.exception.InsufficientSeatsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeatValidatorUtil {

    public void validateAvailability(Fixture fixture, int requestedSeats) {
        int availableSeats = fixture.getAvailableSeats();
        if (availableSeats < requestedSeats) {
            log.warn("Insufficient seats for fixtureId={} — available={}, requested={}",
                    fixture.getId(), availableSeats, requestedSeats);
            throw new InsufficientSeatsException(availableSeats);
        }
    }
}