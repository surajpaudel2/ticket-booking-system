package com.tbs.eventservice.messaging.listener;

import com.tbs.eventservice.messaging.payload.inbound.SeatsReleaseEventPayload;
import com.tbs.eventservice.service.FixtureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Listens for seats.release events published by booking-service.
 * Restores seat count in DB and updates Redis cache when a booking fails or expires.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SeatsReleaseListener {

    private final FixtureService fixtureService;

    // Receives the release event and delegates seat restoration to FixtureService
    @Bean
    public Consumer<SeatsReleaseEventPayload> seatsRelease() {
        return payload -> {
            log.info("Received seatsRelease event: fixtureId={}, seats={}, reason={}",
                    payload.fixtureId(), payload.requestedSeats(), payload.reason());
            fixtureService.releaseSeats(payload.fixtureId(), payload.requestedSeats());
        };
    }
}
