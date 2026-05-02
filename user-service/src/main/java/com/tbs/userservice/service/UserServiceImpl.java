package com.tbs.userservice.service;

import com.tbs.userservice.dto.request.CreateUserRequest;
import com.tbs.userservice.dto.request.UpdateProfileRequest;
import com.tbs.userservice.entity.User;
import com.tbs.userservice.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service after registration
    // ─────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Attempting to create new user with email: {}", request.email());

        validateUserCreationPayload(request);

        User newUser = buildNewUserEntity(request);
        User savedUser = userRepository.save(newUser);

        log.info("Successfully created user ID: {} with role: {}", savedUser.getId(), savedUser.getRole());
        return toResponse(savedUser);
    }

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service during login
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user details for email: {}", email);
        User user = fetchUserEntityByEmail(email);
        return toResponse(user);
    }

    // ─────────────────────────────────────────────
    // CUSTOMER — profile management
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        UUID currentUserId = fetchAuthenticatedUserId();
        log.debug("Fetching profile for current user ID: {}", currentUserId);

        User user = fetchUserEntityById(currentUserId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        UUID currentUserId = fetchAuthenticatedUserId();
        log.info("Attempting to update profile for user ID: {}", currentUserId);

        User user = fetchUserEntityById(currentUserId);
        applyProfileUpdates(user, request);

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user ID: {}", currentUserId);
        return toResponse(updatedUser);
    }

    // ─────────────────────────────────────────────
    // ADMIN — user management
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.debug("Admin fetching user details for ID: {}", userId);
        User user = fetchUserEntityById(userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUserActiveStatus(UUID userId, boolean isActive) {
        log.info("Admin attempting to set active status to {} for user ID: {}", isActive, userId);

        User user = fetchUserEntityById(userId);
        validateNotAdminTarget(user);

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);

        log.info("Successfully set active status to {} for user ID: {}", isActive, userId);
        return toResponse(updatedUser);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS — Validation & Mapping
    // ─────────────────────────────────────────────

    private void validateUserCreationPayload(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("User creation failed: Email {} is already in use", request.email());
            throw new DuplicateUserException("Email already exists: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            log.warn("User creation failed: Username {} is already taken", request.username());
            throw new DuplicateUserException("Username already taken: " + request.username());
        }
    }

    private User buildNewUserEntity(CreateUserRequest request) {
        return User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .role(Role.ROLE_TICKET_HOLDER)
                .active(true)
                .build();
    }

    private void applyProfileUpdates(User user, UpdateProfileRequest request) {
        // 1. Handle standard profile fields
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());

        // 2. Handle the high-risk Email update separately
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            updateUserEmail(user, request.email());
        }
    }

    private void updateUserEmail(User user, String newEmail) {
        log.info("User ID {} is attempting to change email to {}", user.getId(), newEmail);

        // Rule 1: Ensure no one else is already using this new email
        if (userRepository.existsByEmail(newEmail)) {
            log.warn("Email update failed: {} is already taken", newEmail);
            throw new DuplicateUserException("Email already exists: " + newEmail);
        }

        // Rule 2: Update the local database
        user.setEmail(newEmail);

        // Rule 3: TODO - Sync with Auth Service!
        // You MUST notify the auth-service that the credential has changed.
        // syncEmailWithAuthService(user.getId(), newEmail);
    }

    private void validateNotAdminTarget(User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            log.warn("Status update failed: Target user {} is an ADMIN", user.getId());
            throw new UnauthorizedAccessException("Cannot modify status of an admin user");
        }
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS — Data Fetching
    // ─────────────────────────────────────────────

    private User fetchUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User fetch failed: ID {} not found", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });
    }

    private User fetchUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User fetch failed: Email {} not found", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }

    private UUID fetchAuthenticatedUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return UUID.fromString(principal);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS — Transformers
    // ─────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getProfilePictureUrl(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}