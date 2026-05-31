package com.tbs.eventservice.controller;

import com.tbs.eventservice.dto.request.CreateFixtureRequest;
import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ApiResponse;
import com.tbs.eventservice.dto.response.FixtureResponse;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes fixture creation and seat reservation endpoints. */
@RestController
@RequestMapping("/api/v1/events/fixtures")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Fixture", description = "Fixture creation and seat reservation endpoints")
public class FixtureController {

    private final FixtureService fixtureService;

    // ─── FIXTURE ENDPOINTS ───────────────────────────────────────

    /**
     * Creates a new Fixture linked to an existing Season.
     *
     * @param request the validated Fixture creation request
     * @return 201 CREATED with the persisted Fixture as response body
     */
    @PostMapping
    @Operation(
            summary = "Create a fixture",
            description = "Creates a new Fixture linked to an existing Season. " +
                    "Sets both originalScheduledStartTime and currentScheduledStartTime to scheduledStart, " +
                    "and initialises availableSeats to totalSeats."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Fixture created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation failure on request fields"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Season not found for the given seasonId")
    })
    public ResponseEntity<ApiResponse<FixtureResponse>> createFixture(
            @Valid @RequestBody CreateFixtureRequest request) {
        log.info("POST /api/v1/fixtures seasonId={} homeTeam={}", request.getSeasonId(), request.getHomeTeamName());
        FixtureResponse response = fixtureService.createFixture(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fixture created successfully", response));
    }

    // Reserves seats using Redis cache-aside and distributed lock with DB pessimistic double-check
    // TODO : Need to secure with /internal api call.
    @PostMapping("/reserve-seats")
    @Operation(
            summary = "Reserve seats for a fixture",
            description = "Validates fixture exists (Redis cache-aside), performs fast rejection " +
                    "via cached seat count, acquires distributed Redis lock, double-checks seat " +
                    "availability from DB with pessimistic lock, reduces seat count atomically. " +
                    "Called internally by booking-service via Feign only."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Seats reserved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Fixture not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Insufficient seats or Redis lock contention")
    })
    public ResponseEntity<ApiResponse<ReserveSeatResponse>> reserveSeats(
            @Valid @RequestBody ReserveSeatRequest request) {
        log.info("POST /reserve-seats fixtureId={}", request.getFixtureId());
        ReserveSeatResponse response = fixtureService.reserveSeats(request);
        var data = ApiResponse.success("Seats reserved successfully", response);
        log.info("Response data: {}", data, data.data());
        return ResponseEntity.ok(data);
    }
}
