package com.tbs.userservice.controller;

import com.tbs.userservice.dto.request.CreateUserRequest;
import com.tbs.userservice.dto.request.UpdateProfileRequest;
import com.tbs.userservice.dto.response.ApiResponse;
import com.tbs.userservice.dto.response.UserResponse;
import com.tbs.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
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

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/internal/by-email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @RequestParam String email) {

        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    // ─────────────────────────────────────────────
    // CUSTOMER — own profile management
    // ─────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    // ─────────────────────────────────────────────
    // ADMIN — user management
    // ─────────────────────────────────────────────

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId) {

        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PatchMapping("/admin/{userId}/active-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserActiveStatus(
            @PathVariable UUID userId,
            @RequestParam boolean isActive) {

        UserResponse response = userService.updateUserActiveStatus(userId, isActive);
        String message = isActive ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}