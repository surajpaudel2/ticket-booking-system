package com.tbs.bookingservice.controller;

import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.service.BookingService;
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

/** REST controller exposing booking management endpoints to clients and internal services. */
@RestController
@RequestMapping("/api/v1/bookings")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/initiate")
    @Operation(
            summary = "Initiate a booking",
            description = "Reserves seats for a fixture and initiates Stripe payment intent. " +
                    "Returns client secret for frontend Stripe checkout. " +
                    "Booking expires in 15 minutes if payment is not completed."
    )
    @ApiResponse(responseCode = "200", description = "Booking initiated successfully")
    @ApiResponse(responseCode = "404", description = "Fixture not found")
    @ApiResponse(responseCode = "409", description = "Insufficient seats available")
    @ApiResponse(responseCode = "500", description = "Payment initiation failed")
    public ResponseEntity<InitiateBookingResponse> initiateBooking(@Valid @RequestBody InitiateBookingRequest request) {
        log.info("POST /api/v1/bookings/initiate: userId={}, fixtureId={}", request.getUserId(), request.getFixtureId());
        InitiateBookingResponse response = bookingService.initiateBooking(request);
        log.info("Booking initiated successfully: bookingId={}", response.bookingId());
        return ResponseEntity.ok(response);
    }
}
