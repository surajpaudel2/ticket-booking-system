package com.tbs.eventservice.controller;

import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing fixture seat management endpoints consumed internally by booking-service. */
@RestController
@RequestMapping("/api/v1/fixtures")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Fixture", description = "Fixture and seat management endpoints")
public class FixtureController {

    private final FixtureService fixtureService;

    @PostMapping("/reserve-seats")
    @Operation(
            summary = "Reserve seats for a fixture",
            description = "Checks Redis cache for fixture, acquires distributed lock, " +
                    "double checks seat availability from DB, reduces seat count atomically. " +
                    "Called internally by booking-service via Feign."
    )
    @ApiResponse(responseCode = "200", description = "Seats reserved successfully")
    @ApiResponse(responseCode = "404", description = "Fixture not found")
    @ApiResponse(responseCode = "409", description = "Insufficient seats or lock contention")
    public ResponseEntity<ReserveSeatResponse> reserveSeats(@Valid @RequestBody ReserveSeatRequest request) {
        log.info("POST /reserve-seats: fixtureId={}, seats={}", request.getFixtureId(), request.getRequestedSeats());
        ReserveSeatResponse response = fixtureService.reserveSeats(request);
        log.info("Seats reserved: fixtureId={}", request.getFixtureId());
        return ResponseEntity.ok(response);
    }
}
