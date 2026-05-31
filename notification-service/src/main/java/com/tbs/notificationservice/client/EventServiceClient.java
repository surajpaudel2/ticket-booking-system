package com.tbs.notificationservice.client;

import com.tbs.notificationservice.client.dto.response.FixtureResponse;
import com.tbs.notificationservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/v1/events/fixtures/{fixtureId}")
    ApiResponse<FixtureResponse> getFixtureById(@PathVariable Long fixtureId);
}
