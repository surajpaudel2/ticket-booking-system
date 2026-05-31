package com.tbs.eventservice.dto.request;

import com.tbs.eventservice.entity.enums.FixtureClassification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Request body for creating a new Fixture linked to an existing Season. */
@Getter
@Setter
@NoArgsConstructor
public class CreateFixtureRequest {

    /** Name of the home team. */
    @NotBlank
    @Size(max = 100)
    private String homeTeamName;

    /** Name of the away team. */
    @NotBlank
    @Size(max = 100)
    private String awayTeamName;

    /** Name of the venue where the fixture is played. */
    @NotBlank
    @Size(max = 100)
    private String stadiumName;

    /** Classification that determines the fixture's significance (e.g. DERBY, CUP_FINAL). */
    @NotNull
    private FixtureClassification classification;

    /** ID of the Season this fixture belongs to. */
    @NotNull
    private Long seasonId;

    /** Planned kickoff date and time. */
    @NotNull
    private LocalDateTime scheduledStart;

    /** Total number of seats available in the venue for this fixture. */
    @Positive
    private int totalSeats;

    /** Ticket price per seat in the default currency. */
    @Positive
    private double pricePerSeat;
}
