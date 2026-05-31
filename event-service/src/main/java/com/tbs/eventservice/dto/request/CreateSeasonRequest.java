package com.tbs.eventservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Request body for creating a new Season. */
@Getter
@Setter
@NoArgsConstructor
public class CreateSeasonRequest {

    /**
     * Human-readable label for the season (e.g. "2025/26 A-League").
     * Examples: "2025/26 Season", "2025/26 A-League", "Pre-Season 2026".
     */
    @NotBlank
    @Size(max = 100)
    private String seasonName;

    /** Planned start date and time of the season. */
    @NotNull
    private LocalDateTime expectedStartDateTime;

    /** Planned end date and time of the season — must be strictly after expectedStartDateTime. */
    @NotNull
    private LocalDateTime expectedEndDateTime;
}
