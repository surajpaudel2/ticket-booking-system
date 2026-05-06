package com.tbs.userservice.service.impl;

import com.tbs.userservice.dto.request.CreateUserRequest;
import com.tbs.userservice.dto.request.UpdateProfileRequest;
import com.tbs.userservice.dto.response.UserResponse;
import com.tbs.userservice.entity.User;
import com.tbs.userservice.entity.enums.Role;
import com.tbs.userservice.exception.DuplicateUserException;
import com.tbs.userservice.exception.UnauthorizedAccessException;
import com.tbs.userservice.exception.UserNotFoundException;
import com.tbs.userservice.repository.UserRepository;
import com.tbs.userservice.service.UserCacheService;
import com.tbs.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service after registration
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Attempting to create new user: email={}", request.email());

        validateUserCreationPayload(request);

        User savedUser = userRepository.save(buildNewUserEntity(request));

        log.info("User created successfully: userId={}, role={}", savedUser.getId(), savedUser.getRole());
        return toResponse(savedUser);
    }

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service during login
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: email={}", email);

        UserResponse cachedProfile = userCacheService.getUserByEmail(email);
        if (cachedProfile != null) {
            log.debug("Cache hit for email: email={}", email);
            return cachedProfile;
        }

        log.debug("Cache miss for email - fetching from DB: email={}", email);
        User user = fetchUserEntityByEmail(email);
        UserResponse response = toResponse(user);

        userCacheService.saveUser(response);
        log.debug("User fetched from DB and cached: email={}, userId={}", email, response.id());
        return response;
    }

    // ─────────────────────────────────────────────
    // Customer — profile management
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        UUID currentUserId = fetchAuthenticatedUserId();
        log.info("Fetching current user profile: userId={}", currentUserId);

        UserResponse response = getUserById(currentUserId);

        log.info("Current user profile retrieved successfully: userId={}", currentUserId);
        return response;
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        UUID currentUserId = fetchAuthenticatedUserId();
        log.info("Attempting to update profile: userId={}", currentUserId);

        User user = fetchUserEntityById(currentUserId);
        applyProfileUpdates(user, request);

        User updatedUser = userRepository.save(user);
        UserResponse response = toResponse(updatedUser);

        userCacheService.saveUser(response);
        log.info("Profile updated successfully: userId={}", currentUserId);
        return response;
    }

    // ─────────────────────────────────────────────
    // ADMIN — user management
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.debug("Fetching user by ID: userId={}", userId);

        UserResponse cachedProfile = userCacheService.getUserById(userId);
        if (cachedProfile != null) {
            log.debug("Cache hit for userId: userId={}", userId);
            return cachedProfile;
        }

        log.debug("Cache miss for userId - fetching from DB: userId={}", userId);
        User user = fetchUserEntityById(userId);
        UserResponse response = toResponse(user);

        userCacheService.saveUser(response);
        log.debug("User fetched from DB and cached: userId={}", userId);
        return response;
    }

    @Override
    @Transactional
    public UserResponse updateUserActiveStatus(UUID userId, boolean isActive) {
        log.info("Admin requesting to {} user: userId={}", isActive ? "activate" : "deactivate", userId);

        User user = fetchUserEntityById(userId);
        validateNotAdminTarget(user);

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        UserResponse response = toResponse(updatedUser);

        userCacheService.saveUser(response);
        log.info("User active status updated successfully: userId={}, isActive={}", userId, isActive);
        return response;
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private void validateUserCreationPayload(CreateUserRequest request) {
        log.debug("Validating user creation payload: email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("User creation failed - email already exists: email={}", request.email());
            throw new DuplicateUserException("Email already exists: " + request.email());
        }
    }

    private User buildNewUserEntity(CreateUserRequest request) {
        log.debug("Building new user entity: email={}, role={}", request.email(), Role.ROLE_CUSTOMER);

        return User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .role(Role.ROLE_CUSTOMER)
                .active(true)
                .build();
    }

    private void applyProfileUpdates(User user, UpdateProfileRequest request) {
        log.debug("Applying profile updates for userId={}", user.getId());

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            updateUserEmail(user, request.email());
        }
    }

    private void updateUserEmail(User user, String newEmail) {
        log.info("Email change requested: userId={}", user.getId());

        if (userRepository.existsByEmail(newEmail)) {
            log.warn("Email change failed - new email already in use: userId={}", user.getId());
            throw new DuplicateUserException("Email already exists: " + newEmail);
        }

        userCacheService.evictUserByEmail(user.getEmail());
        user.setEmail(newEmail);

        log.info("Email updated and old cache evicted: userId={}", user.getId());

        // TODO: Sync with Auth Service
    }

    private void validateNotAdminTarget(User user) {
        log.debug("Validating admin status modification target: userId={}, role={}", user.getId(), user.getRole());

        if (user.getRole() == Role.ROLE_ADMIN) {
            log.warn("Unauthorized attempt to modify admin user status: userId={}", user.getId());
            throw new UnauthorizedAccessException("Cannot modify status of an admin user");
        }
    }

    private User fetchUserEntityById(UUID userId) {
        log.debug("Querying DB for user by ID: userId={}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found in DB: userId={}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });
    }

    private User fetchUserEntityByEmail(String email) {
        log.debug("Querying DB for user by email: email={}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found in DB: email={}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }

    private UUID fetchAuthenticatedUserId() {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        log.debug("Authenticated userId resolved from SecurityContext: userId={}", userId);
        return userId;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getDateOfBirth(),
                user.getProfilePictureUrl(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}