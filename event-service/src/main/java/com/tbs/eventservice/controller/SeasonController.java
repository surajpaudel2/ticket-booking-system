package com.tbs.eventservice.controller;

import com.tbs.eventservice.dto.request.CreateSeasonRequest;
import com.tbs.eventservice.dto.response.ApiResponse;
import com.tbs.eventservice.dto.response.SeasonResponse;
import com.tbs.eventservice.service.SeasonService;
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

/** Exposes Season management endpoints. */
@RestController
@RequestMapping("/api/v1/events/seasons")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Season", description = "Season lifecycle management endpoints")
public class SeasonController {

    private final SeasonService seasonService;

    // ─── SEASON ENDPOINTS ────────────────────────────────────────

    /**
     * Creates a new Season.
     *
     * @param request the validated Season creation request
     * @return 201 CREATED with the persisted Season as response body
     */
    @PostMapping
    @Operation(
            summary = "Create a season",
            description = "Creates a new Season with the given name and expected date range. " +
                    "Validates that expectedStartDateTime is strictly before expectedEndDateTime."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Season created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation failure or invalid date range (start not before end)")
    })
    public ResponseEntity<ApiResponse<SeasonResponse>> createSeason(
            @Valid @RequestBody CreateSeasonRequest request) {
        log.info("POST /api/seasons seasonName={}", request.getSeasonName());
        SeasonResponse response = seasonService.createSeason(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Season created successfully", response));
    }
}
