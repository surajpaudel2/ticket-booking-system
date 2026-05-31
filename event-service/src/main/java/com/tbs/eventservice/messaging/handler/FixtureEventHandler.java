package com.tbs.eventservice.messaging.handler;

import com.tbs.eventservice.messaging.payload.inbound.SeatsReleaseEventPayload;
import com.tbs.eventservice.service.FixtureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FixtureEventHandler {

    private final FixtureService fixtureService;

    public void handleSeatsRelease(SeatsReleaseEventPayload payload) {
        log.info("Received seatsRelease fixtureId={} bookingId={} seats={} reason={}",
                payload.fixtureId(), payload.bookingId(), payload.requestedSeats(), payload.reason());
        fixtureService.releaseSeats(payload.fixtureId(), payload.requestedSeats());
        log.info("Seats release completed fixtureId={}", payload.fixtureId());
    }
}
