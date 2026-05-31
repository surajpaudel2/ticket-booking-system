package com.tbs.eventservice.messaging.consumer;

import com.tbs.eventservice.messaging.handler.FixtureEventHandler;
import com.tbs.eventservice.messaging.payload.inbound.SeatsReleaseEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class SeatsEventConsumer  {

    private final FixtureEventHandler fixtureEventHandler;

    @Bean
    public Consumer<SeatsReleaseEventPayload> seatsRelease() {
        return fixtureEventHandler::handleSeatsRelease;
    }
}
