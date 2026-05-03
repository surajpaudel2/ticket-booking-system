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
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        saveCredential(request);
        createUserProfile(request);
        log.info("User registered: {}", request.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Credential credential = findCredentialOrThrow(request.getEmail());
        verifyPasswordOrThrow(request.getPassword(), credential.getPassword());
        UserResponse userProfile = fetchUserProfile(request.getEmail());
        String accessToken = generateAccessToken(userProfile);
        RefreshToken refreshToken = createAndSaveRefreshToken(request.getEmail());
        log.info("User logged in: {}", request.getEmail());
        return buildAuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = findRefreshTokenOrThrow(request.getRefreshToken());
        validateRefreshToken(storedToken);
        UserResponse userProfile = fetchUserProfile(storedToken.getEmail());
        String newAccessToken = generateAccessToken(userProfile);
        revokeToken(storedToken);
        RefreshToken newRefreshToken = createAndSaveRefreshToken(storedToken.getEmail());
        log.info("Token refreshed for: {}", storedToken.getEmail());
        return buildAuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.revokeAllByEmail(email);
        log.info("User logged out: {}", email);
    }

    private Credential saveCredential(RegisterRequest request) {
        Credential credential = Credential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        return credentialRepository.save(credential);
    }

    private void createUserProfile(RegisterRequest request) {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.getEmail(), request.getFullName(), request.getPassword()
        );
        userServiceClient.createUser(createUserRequest);
    }

    private Credential findCredentialOrThrow(String email) {
        return credentialRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private void verifyPasswordOrThrow(String rawPassword, String hashedPassword) {
        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            throw new InvalidCredentialsException();
        }
    }

    private UserResponse fetchUserProfile(String email) {
        return userServiceClient.getUserByEmail(email).data();
    }

    private String generateAccessToken(UserResponse userProfile) {
        return jwtTokenProvider.generateAccessToken(
                userProfile.id().toString(),
                userProfile.email(),
                userProfile.role()
        );
    }

    private RefreshToken createAndSaveRefreshToken(String email) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(String accessToken, RefreshToken refreshToken) {
        long expiresInSeconds = jwtTokenProvider.getAccessTokenExpiryMs() / 1000;
        return AuthResponse.of(accessToken, refreshToken.getToken(), expiresInSeconds);
    }

    private RefreshToken findRefreshTokenOrThrow(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    }

    private void validateRefreshToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (token.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }
    }
}
