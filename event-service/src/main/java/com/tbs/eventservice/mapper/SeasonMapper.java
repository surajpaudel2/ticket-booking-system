package com.tbs.eventservice.mapper;

import com.tbs.eventservice.dto.request.CreateSeasonRequest;
import com.tbs.eventservice.dto.response.SeasonResponse;
import com.tbs.eventservice.entity.Season;
import org.springframework.stereotype.Component;

/** Manual mapper between Season entity and its DTOs. */
@Component
public class SeasonMapper {

    /**
     * Maps a {@link CreateSeasonRequest} to a new {@link Season} entity (without ID or audit fields).
     *
     * @param request the validated creation request
     * @return a transient Season ready for persistence
     */
    public Season toEntity(CreateSeasonRequest request) {
        Season season = new Season();
        season.setSeasonName(request.getSeasonName());
        season.setExpectedStartDateTime(request.getExpectedStartDateTime());
        season.setExpectedEndDateTime(request.getExpectedEndDateTime());
        return season;
    }

    /**
     * Maps a persisted {@link Season} to a {@link SeasonResponse}.
     *
     * @param season the persisted Season entity
     * @return the response DTO for the caller
     */
    public SeasonResponse toResponse(Season season) {
        return SeasonResponse.builder()
                .id(season.getId())
                .seasonName(season.getSeasonName())
                .expectedStartDateTime(season.getExpectedStartDateTime())
                .expectedEndDateTime(season.getExpectedEndDateTime())
                .startedAt(season.getStartedAt())
                .finishedAt(season.getFinishedAt())
                .createdAt(season.getCreatedAt())
                .build();
    }
}
