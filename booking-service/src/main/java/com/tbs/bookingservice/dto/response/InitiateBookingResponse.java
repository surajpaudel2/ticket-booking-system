package com.tbs.bookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Response returned to the caller after a booking is successfully initiated and payment intent created. */
public record InitiateBookingResponse(
        Long bookingId,
        Long fixtureId,
        String paymentIntentId,
        String clientSecret,
        double totalAmount,
        int requestedSeats,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime expiresAt,
        String status
) {}
