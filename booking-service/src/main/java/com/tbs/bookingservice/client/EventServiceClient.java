package com.tbs.bookingservice.client;

import com.tbs.bookingservice.client.dto.request.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.response.ReserveSeatResponse;
import com.tbs.bookingservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Event Service.
 * Used to reserve seats for a fixture during booking initiation.
 */
@FeignClient(name = "event-service")
public interface EventServiceClient {

    // Calls POST /api/v1/events/fixtures/reserve-seats on event-service
    @PostMapping("/api/v1/events/fixtures/reserve-seats")
    ApiResponse<ReserveSeatResponse> reserveSeats(@RequestBody ReserveSeatRequest request);
}
