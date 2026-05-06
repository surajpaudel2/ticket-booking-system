package com.tbs.userservice.controller;

import com.tbs.userservice.dto.request.CreateUserRequest;
import com.tbs.userservice.dto.request.UpdateProfileRequest;
import com.tbs.userservice.dto.response.ApiResponse;
import com.tbs.userservice.dto.response.UserResponse;
import com.tbs.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management.
 *
 * Security layers:
 *   - /internal/**  → restricted to internal service-to-service calls (auth-service)
 *   - /me/**        → any authenticated user (their own profile)
 *   - /admin/**     → ROLE_ADMIN only
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service
    // ─────────────────────────────────────────────

    @PostMapping("/internal")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody CreateUserRequest request) {

        log.info("Internal request received to create user: email={}", request.email());

        UserResponse response = userService.createUser(request);

        log.info("User created successfully via internal call: email={}, userId={}", request.email(), response.id());

        return ResponseEntity.status(201)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/admin/by-email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @RequestParam String email) {

        log.info("Internal request received to fetch user by email: email={}", email);

        UserResponse response = userService.getUserByEmail(email);

        log.info("User fetched successfully by email: email={}, userId={}", email, response.id());

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    // ─────────────────────────────────────────────
    // CUSTOMER — own profile management
    // ─────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {

        log.info("Authenticated user requesting their own profile");

        UserResponse response = userService.getCurrentUserProfile();

        log.info("Profile retrieved successfully for userId={}", response.id());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @RequestBody UpdateProfileRequest request) {

        log.info("Authenticated user requesting profile update");

        UserResponse response = userService.updateCurrentUserProfile(request);

        log.info("Profile updated successfully for userId={}", response.id());

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    // ─────────────────────────────────────────────
    // ADMIN — user management
    // ─────────────────────────────────────────────

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId) {

        log.info("Admin request to fetch user by ID: userId={}", userId);

        UserResponse response = userService.getUserById(userId);

        log.info("User fetched successfully by admin: userId={}, email={}", userId, response.email());

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PatchMapping("/admin/{userId}/active-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserActiveStatus(
            @PathVariable UUID userId,
            @RequestParam boolean isActive) {

        log.info("Admin request to {} user: userId={}", isActive ? "activate" : "deactivate", userId);

        UserResponse response = userService.updateUserActiveStatus(userId, isActive);
        String message = isActive ? "User activated successfully" : "User deactivated successfully";

        log.info("User active status updated successfully: userId={}, isActive={}", userId, isActive);

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}