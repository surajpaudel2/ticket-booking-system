package com.tbs.eventservice.mapper;

import com.tbs.eventservice.dto.request.CreateFixtureRequest;
import com.tbs.eventservice.dto.response.FixtureResponse;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.entity.Fixture;
import com.tbs.eventservice.entity.Season;
import org.springframework.stereotype.Component;

@Component
public class FixtureMapper {

    public Fixture toEntity(CreateFixtureRequest request, Season season) {
        return Fixture.builder()
                .homeTeamName(request.getHomeTeamName())
                .awayTeamName(request.getAwayTeamName())
                .stadiumName(request.getStadiumName())
                .classification(request.getClassification())
                .season(season)
                .originalScheduledStartTime(request.getScheduledStart())
                .currentScheduledStartTime(request.getScheduledStart())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats()) // all seats available at fixture creation
                .nextSeat(1)                             // seat allocation always begins from seat 1
                .pricePerSeat(request.getPricePerSeat())
                .build();
    }

    public FixtureResponse toFixtureResponse(Fixture fixture) {
        return FixtureResponse.builder()
                .id(fixture.getId())
                .homeTeamName(fixture.getHomeTeamName())
                .awayTeamName(fixture.getAwayTeamName())
                .stadiumName(fixture.getStadiumName())
                .classification(fixture.getClassification())
                .seasonId(fixture.getSeason().getId())
                .seasonName(fixture.getSeason().getSeasonName())
                .totalSeats(fixture.getTotalSeats())
                .availableSeats(fixture.getAvailableSeats())
                .pricePerSeat(fixture.getPricePerSeat())
                .originalScheduledStartTime(fixture.getOriginalScheduledStartTime())
                .currentScheduledStartTime(fixture.getCurrentScheduledStartTime())
                .createdAt(fixture.getCreatedAt())
                .build();
    }

    public ReserveSeatResponse toReserveSeatResponse(Fixture fixture, int seatsReserved) {
        return new ReserveSeatResponse(
                fixture.getId(),
                fixture.getHomeTeamName(),
                fixture.getAwayTeamName(),
                fixture.getStadiumName(),
                fixture.getCurrentScheduledStartTime(),
                fixture.getPricePerSeat(),
                seatsReserved,
                fixture.getAvailableSeats());
    }
}