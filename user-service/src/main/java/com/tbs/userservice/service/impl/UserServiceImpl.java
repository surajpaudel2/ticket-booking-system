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
        log.info("Attempting to create new user with email: {}", request.email());
        validateUserCreationPayload(request);

        User savedUser = userRepository.save(buildNewUserEntity(request));

        log.info("Successfully created user ID: {} with role: {}", savedUser.getId(), savedUser.getRole());
        return toResponse(savedUser);
    }

    // ─────────────────────────────────────────────
    // INTERNAL — called by auth-service during login
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        UserResponse cachedProfile = userCacheService.getUserByEmail(email);
        if (cachedProfile != null) {
            return cachedProfile;
        }

        User user = fetchUserEntityByEmail(email);
        UserResponse response = toResponse(user);

        userCacheService.saveUser(response);
        return response;
    }

    // ─────────────────────────────────────────────
    // Ticket Holder — profile management
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        return getUserById(fetchAuthenticatedUserId());
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        UUID currentUserId = fetchAuthenticatedUserId();
        log.info("Attempting to update profile for user ID: {}", currentUserId);

        User user = fetchUserEntityById(currentUserId);
        applyProfileUpdates(user, request);

        User updatedUser = userRepository.save(user);
        UserResponse response = toResponse(updatedUser);

        userCacheService.saveUser(response);
        log.info("Successfully updated profile for user ID: {}", currentUserId);
        return response;
    }

    // ─────────────────────────────────────────────
    // ADMIN — user management
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        UserResponse cachedProfile = userCacheService.getUserById(userId);
        if (cachedProfile != null) {
            return cachedProfile;
        }

        User user = fetchUserEntityById(userId);
        UserResponse response = toResponse(user);

        userCacheService.saveUser(response);
        return response;
    }

    @Override
    @Transactional
    public UserResponse updateUserActiveStatus(UUID userId, boolean isActive) {
        log.info("Admin attempting to set active status to {} for user ID: {}", isActive, userId);

        User user = fetchUserEntityById(userId);
        validateNotAdminTarget(user);

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        UserResponse response = toResponse(updatedUser);

        userCacheService.saveUser(response);
        log.info("Successfully set active status to {} for user ID: {}", isActive, userId);
        return response;
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private void validateUserCreationPayload(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists: " + request.email());
        }
    }

    private User buildNewUserEntity(CreateUserRequest request) {
        return User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .role(Role.ROLE_CUSTOMER)
                .active(true)
                .build();
    }

    private void applyProfileUpdates(User user, UpdateProfileRequest request) {
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            updateUserEmail(user, request.email());
        }
    }

    private void updateUserEmail(User user, String newEmail) {
        log.info("User ID {} is changing email from {} to {}", user.getId(), user.getEmail(), newEmail);

        if (userRepository.existsByEmail(newEmail)) {
            throw new DuplicateUserException("Email already exists: " + newEmail);
        }

        userCacheService.evictUserByEmail(user.getEmail());
        user.setEmail(newEmail);

        // TODO: Sync with Auth Service
    }

    private void validateNotAdminTarget(User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("Cannot modify status of an admin user");
        }
    }

    private User fetchUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private User fetchUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private UUID fetchAuthenticatedUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
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