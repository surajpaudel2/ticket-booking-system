package com.tbs.eventservice.dto.response;

import com.tbs.eventservice.entity.enums.FixtureClassification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Represents a persisted Fixture returned to the caller. */
@Getter
@Builder
public class FixtureResponse {

    /** Database-assigned primary key. */
    private Long id;

    /** Name of the home team. */
    private String homeTeamName;

    /** Name of the away team. */
    private String awayTeamName;

    /** Name of the venue. */
    private String stadiumName;

    /** Classification indicating the fixture's significance. */
    private FixtureClassification classification;

    /** ID of the Season this fixture belongs to. */
    private Long seasonId;

    /** Human-readable label of the parent Season. */
    private String seasonName;

    /** Total seat capacity of the venue for this fixture. */
    private int totalSeats;

    /** Current number of seats still open for booking. */
    private int availableSeats;

    /** Ticket price per seat. */
    private double pricePerSeat;

    /** The first planned kickoff time — never changed after creation. */
    private LocalDateTime originalScheduledStartTime;

    /** The current planned kickoff time — updated on postponement. */
    private LocalDateTime currentScheduledStartTime;

    /** Timestamp at which this Fixture record was first persisted. */
    private LocalDateTime createdAt;
}
