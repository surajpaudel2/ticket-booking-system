package com.tbs.eventservice.messaging.listener;

import com.tbs.eventservice.messaging.payload.inbound.SeatsReleaseEventPayload;
import com.tbs.eventservice.service.FixtureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Listens for seats.release events from booking-service.
 * Seat release is processed asynchronously — booking-service sends the event
 * and returns to the caller immediately without waiting for this listener.
 * Spring Cloud Stream manages the consumer thread separately from the HTTP thread.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SeatsReleaseListener {

    private final FixtureService fixtureService;

    // Consumes seats.release events and delegates to FixtureService for atomic DB restoration
    @Bean
    public Consumer<SeatsReleaseEventPayload> seatsRelease() {
        return payload -> {
            log.info("Received seatsRelease: fixtureId={} seats={} reason={}",
                    payload.fixtureId(), payload.requestedSeats(), payload.reason());
            fixtureService.releaseSeats(payload.fixtureId(), payload.requestedSeats());
            log.info("Seats release completed for fixtureId={}", payload.fixtureId());
        };
    }
}
