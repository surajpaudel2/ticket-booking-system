package com.tbs.userservice.service;

import com.tbs.userservice.dto.request.CreateUserRequest;
import com.tbs.userservice.dto.request.UpdateProfileRequest;
import com.tbs.userservice.dto.response.UserResponse;

import java.util.UUID;

/**
 * Service interface for managing user profiles and administrative actions.
 */
public interface UserService {

    /**
     * Internal: Creates a new user record after registration.
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Internal: Retrieves user details by email, typically for authentication.
     */
    UserResponse getUserByEmail(String email);

    /**
     * Ticket Holder & Admin: Retrieves the profile of the currently authenticated user.
     */
    UserResponse getCurrentUserProfile();

    /**
     * Ticket Holder & Admin: Updates the profile details for the currently authenticated user.
     */
    UserResponse updateCurrentUserProfile(UpdateProfileRequest request);

    /**
     * Admin: Retrieves a specific user profile by their unique ID.
     */
    UserResponse getUserById(UUID userId);

    /**
     * Admin: Toggles a user's account status (Active/Inactive).
     */
    UserResponse updateUserActiveStatus(UUID userId, boolean isActive);
}