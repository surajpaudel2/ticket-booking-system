package com.tbs.bookingservice.service;

import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;

/** Orchestrates the full booking initiation flow — seat reservation, payment, and event publishing. */
public interface BookingService {

    /** Reserves seats, initiates payment, and returns Stripe checkout credentials. */
    InitiateBookingResponse initiateBooking(InitiateBookingRequest request);
}
