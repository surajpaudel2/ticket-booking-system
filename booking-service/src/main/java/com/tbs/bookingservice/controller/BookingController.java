package com.tbs.bookingservice.controller;

import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.ApiResponse;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.service.BookingService;
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

/** Exposes booking initiation endpoints for the Sports Ticketing Platform. */
@RestController
@RequestMapping("/api/v1/bookings")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management for the Sports Ticketing Platform")
public class BookingController {

    private final BookingService bookingService;

    // Delegates booking initiation to service and wraps result in ApiResponse
    @PostMapping("/initiate")
    @Operation(
            summary = "Initiate a booking",
            description = "Reserves seats for a fixture and initiates a Stripe PaymentIntent. " +
                    "Returns clientSecret for frontend Stripe checkout. Booking window is 15 minutes. " +
                    "If payment is not completed within 15 minutes, seats are automatically released."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Booking initiated — Stripe checkout ready"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation error in request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Fixture not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Insufficient seats available or lock contention"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                    description = "Payment initiation failed")
    })
    public ResponseEntity<ApiResponse<InitiateBookingResponse>> initiateBooking(
            @Valid @RequestBody InitiateBookingRequest request) {
        log.info("POST /initiate received fixtureId={} userId={}", request.getFixtureId(), request.getUserId());
        InitiateBookingResponse response = bookingService.initiateBooking(request);
        return ResponseEntity.ok(ApiResponse.success("Booking initiated successfully", response));
    }
}
