package com.tbs.eventservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Represents a persisted Season returned to the caller. */
@Getter
@Builder
public class SeasonResponse {

    /** Database-assigned primary key. */
    private Long id;

    /** Human-readable label for this season (e.g. "2025/26 A-League"). */
    private String seasonName;

    /** Planned start date and time of the season. */
    private LocalDateTime expectedStartDateTime;

    /** Planned end date and time of the season. */
    private LocalDateTime expectedEndDateTime;

    /** Timestamp set when the season officially began; null if not yet started. */
    private LocalDateTime startedAt;

    /** Timestamp set when the season officially concluded; null if not yet finished. */
    private LocalDateTime finishedAt;

    /** Timestamp at which this Season record was first persisted. */
    private LocalDateTime createdAt;
}
