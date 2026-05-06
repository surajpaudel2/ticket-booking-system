package com.tbs.authservice.service.impl;

import com.tbs.authservice.client.UserServiceClient;
import com.tbs.authservice.client.dto.request.CreateUserRequest;
import com.tbs.authservice.client.dto.response.UserResponse;
import com.tbs.authservice.dto.request.LoginRequest;
import com.tbs.authservice.dto.request.RefreshTokenRequest;
import com.tbs.authservice.dto.request.RegisterRequest;
import com.tbs.authservice.dto.response.AuthResponse;
import com.tbs.authservice.entity.Credential;
import com.tbs.authservice.entity.RefreshToken;
import com.tbs.authservice.exception.EmailAlreadyExistsException;
import com.tbs.authservice.exception.InvalidCredentialsException;
import com.tbs.authservice.exception.InvalidTokenException;
import com.tbs.authservice.repository.CredentialRepository;
import com.tbs.authservice.repository.RefreshTokenRepository;
import com.tbs.authservice.security.JwtTokenProvider;
import com.tbs.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final CredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

    @Value("${jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        if (credentialRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        saveCredential(request);

        // can create an event to register user in the user service. For simplicity, we will call the user service directly here.
        createUserProfile(request);

        log.info("User registered successfully: {}", request.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        Credential credential = findCredentialOrThrow(request.getEmail());
        verifyPasswordOrThrow(request.getPassword(), credential.getPassword());

        UserResponse userProfile = fetchUserProfile(request.getEmail());
        String accessToken = generateAccessToken(userProfile);
        RefreshToken refreshToken = createAndSaveRefreshToken(request.getEmail());

        log.info("User logged in successfully: email={}, userId={}, role={}",
                request.getEmail(), userProfile.id(), userProfile.role());

        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        RefreshToken storedToken = findRefreshTokenOrThrow(request.getRefreshToken());

        log.debug("Refresh token found for email: {}, expiresAt: {}",
                storedToken.getEmail(), storedToken.getExpiresAt());

        validateRefreshToken(storedToken);

        UserResponse userProfile = fetchUserProfile(storedToken.getEmail());
        String newAccessToken = generateAccessToken(userProfile);
        revokeToken(storedToken);
        RefreshToken newRefreshToken = createAndSaveRefreshToken(storedToken.getEmail());

        log.info("Token refreshed successfully: email={}, userId={}",
                storedToken.getEmail(), userProfile.id());

        return buildAuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String email) {
        log.info("Logout attempt for email: {}", email);

        refreshTokenRepository.revokeAllByEmail(email);

        log.info("User logged out successfully, all refresh tokens revoked: {}", email);
    }

    private Credential saveCredential(RegisterRequest request) {
        log.debug("Saving credential for email: {}", request.getEmail());

        Credential credential = Credential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        Credential saved = credentialRepository.save(credential);

        log.debug("Credential saved successfully for email: {}", request.getEmail());
        return saved;
    }

    private void createUserProfile(RegisterRequest request) {
        log.debug("Creating user profile via user-service for email: {}", request.getEmail());

        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.getEmail(), request.getFullName(), request.getPassword()
        );
        userServiceClient.createUser(createUserRequest);

        log.debug("User profile created successfully in user-service for email: {}", request.getEmail());
    }

    private Credential findCredentialOrThrow(String email) {
        log.debug("Looking up credential for email: {}", email);

        return credentialRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - no credential found for email: {}", email);
                    return new InvalidCredentialsException();
                });
    }

    private void verifyPasswordOrThrow(String rawPassword, String hashedPassword) {
        log.debug("Verifying password");

        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            log.warn("Login failed - password mismatch");
            throw new InvalidCredentialsException();
        }

        log.debug("Password verification passed");
    }

    private UserResponse fetchUserProfile(String email) {
        log.debug("Fetching user profile from user-service for email: {}", email);

        UserResponse userResponse = userServiceClient.getUserByEmail(email).data();

        log.debug("User profile fetched successfully: email={}, userId={}", email, userResponse.id());
        return userResponse;
    }

    private String generateAccessToken(UserResponse userProfile) {
        log.debug("Generating access token for userId: {}, role: {}", userProfile.id(), userProfile.role());

        String token = jwtTokenProvider.generateAccessToken(
                userProfile.id().toString(),
                userProfile.email(),
                userProfile.role()
        );

        log.debug("Access token generated successfully for userId: {}", userProfile.id());
        return token;
    }

    private RefreshToken createAndSaveRefreshToken(String email) {
        log.debug("Creating refresh token for email: {}", email);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .revoked(false)
                .build();
        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token created and persisted for email: {}, expiresAt: {}",
                email, saved.getExpiresAt());
        return saved;
    }

    private void revokeToken(RefreshToken token) {
        log.debug("Revoking existing refresh token for email: {}", token.getEmail());

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        log.debug("Refresh token revoked for email: {}", token.getEmail());
    }

    private AuthResponse buildAuthResponse(String accessToken, RefreshToken refreshToken) {
        long expiresInSeconds = jwtTokenProvider.getAccessTokenExpiryMs() / 1000;

        log.debug("Building auth response: expiresInSeconds={}", expiresInSeconds);

        return AuthResponse.of(accessToken, refreshToken.getToken(), expiresInSeconds);
    }

    private RefreshToken findRefreshTokenOrThrow(String token) {
        log.debug("Looking up refresh token");

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed - refresh token not found");
                    return new InvalidTokenException("Refresh token not found");
                });
    }

    private void validateRefreshToken(RefreshToken token) {
        log.debug("Validating refresh token for email: {}", token.getEmail());

        if (token.isRevoked()) {
            log.warn("Token refresh failed - token already revoked for email: {}", token.getEmail());
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (token.isExpired()) {
            log.warn("Token refresh failed - token expired for email: {}, expiredAt: {}",
                    token.getEmail(), token.getExpiresAt());
            throw new InvalidTokenException("Refresh token has expired");
        }

        log.debug("Refresh token validation passed for email: {}", token.getEmail());
    }
}