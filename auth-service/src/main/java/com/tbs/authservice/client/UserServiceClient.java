package com.tbs.authservice.client;

import com.tbs.authservice.client.dto.request.CreateUserRequest;
import com.tbs.authservice.client.dto.response.UserResponse;
import com.tbs.authservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceClient {

    @PostMapping("/internal")
    ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request);

    // TODO: Might need to update the url in future.
    @GetMapping("/by-email")
    ApiResponse<UserResponse> getUserByEmail(@RequestParam String email);
}
