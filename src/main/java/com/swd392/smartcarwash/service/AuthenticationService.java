package com.swd392.smartcarwash.service;

import com.swd392.smartcarwash.dto.request.auth.*;
import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.dto.response.MessageResponse;

public interface AuthenticationService {

    MessageResponse register(RegisterRequest request);

    LoginResponse verifyEmail(VerifyOtpRequest request);

    MessageResponse resendOtp(ResendOtpRequest request);

    LoginResponse login(LoginRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    LoginResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(ChangePasswordRequest request);

    MessageResponse logout(String accessToken);

    LoginResponse loginWithGoogle(
            String email,
            String fullName,
            String avatarUrl,
            String providerId
    );
}