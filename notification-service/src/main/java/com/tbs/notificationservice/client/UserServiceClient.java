package com.tbs.notificationservice.client;

import com.tbs.notificationservice.client.dto.response.UserResponse;
import com.tbs.notificationservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable Long userId);
}
