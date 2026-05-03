package com.tbs.authservice.client.dto.request;

public record CreateUserRequest(String email, String fullName, String password) {}
