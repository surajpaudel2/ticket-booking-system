package com.tbs.userservice.service;

import com.tbs.userservice.dto.response.UserResponse;
import java.util.UUID;

/**
 * Interface for managing cached user profile data.
 * Supports dual-key lookups by ID and Email.
 */
public interface UserCacheService {

    /**
     * Retrieves a user from the cache using their unique ID.
     */
    UserResponse getUserById(UUID userId);

    /**
     * Retrieves a user from the cache using their email address.
     */
    UserResponse getUserByEmail(String email);

    /**
     * Persists the user response in the cache under both ID and Email keys.
     */
    void saveUser(UserResponse response);

    /**
     * Explicitly removes a user entry from the cache by email.
     */
    void evictUserByEmail(String email);
}