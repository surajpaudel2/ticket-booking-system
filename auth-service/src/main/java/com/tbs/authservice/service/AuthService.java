package com.tbs.authservice.service;

import com.tbs.authservice.dto.request.LoginRequest;
import com.tbs.authservice.dto.request.RefreshTokenRequest;
import com.tbs.authservice.dto.request.RegisterRequest;
import com.tbs.authservice.dto.response.AuthResponse;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String email);
}
