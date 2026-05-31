package com.tbs.notificationservice.client.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FixtureResponse(
        Long fixtureId,
        Integer availableSeats,
        String homeTeamName,
        String awayTeamName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fixtureDateTime
) {}
