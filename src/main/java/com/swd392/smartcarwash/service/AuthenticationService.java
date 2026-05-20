package com.swd392.smartcarwash.service;

import com.swd392.smartcarwash.dto.request.auth.LoginRequest;
import com.swd392.smartcarwash.dto.request.auth.RegisterRequest;
import com.swd392.smartcarwash.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse loginWithGoogle(
            String email,
            String fullName,
            String avatarUrl,
            String providerId
    );

    void logout(String accessToken);
}