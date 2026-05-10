package com.tbs.bookingservice.client;

import com.tbs.bookingservice.client.dto.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.ReserveSeatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Event Service.
 * Used to reserve seats for a fixture during booking initiation.
 */
@FeignClient(name = "event-service")
public interface EventServiceClient {

    // Calls POST /api/v1/fixtures/reserve-seats on event-service
    @PostMapping("/api/v1/fixtures/reserve-seats")
    ReserveSeatResponse reserveSeats(@RequestBody ReserveSeatRequest request);
}
