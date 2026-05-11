package com.tbs.eventservice.controller;

import com.tbs.eventservice.dto.request.ReserveSeatRequest;
import com.tbs.eventservice.dto.response.ApiResponse;
import com.tbs.eventservice.dto.response.ReserveSeatResponse;
import com.tbs.eventservice.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes fixture seat reservation endpoints — consumed internally by booking-service via Feign. */
@RestController
@RequestMapping("/api/v1/fixtures")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Fixture", description = "Fixture and seat reservation endpoints — internal use by booking-service")
public class FixtureController {

    private final FixtureService fixtureService;

    // Reserves seats using Redis cache-aside and distributed lock with DB pessimistic double-check
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
        return ResponseEntity.ok(ApiResponse.success("Seats reserved successfully", response));
    }
}
