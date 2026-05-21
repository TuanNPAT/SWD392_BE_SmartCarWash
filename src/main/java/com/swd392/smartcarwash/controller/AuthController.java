package com.swd392.smartcarwash.controller;

import com.swd392.smartcarwash.annotation.PublicEndpoint;
import com.swd392.smartcarwash.dto.request.auth.*;
import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.dto.response.MessageResponse;
import com.swd392.smartcarwash.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @PublicEndpoint
    public MessageResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    @PublicEndpoint
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authenticationService.login(request);
    }

    @PostMapping("/verify-email")
    @PublicEndpoint
    public LoginResponse verifyEmail(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        return authenticationService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    @PublicEndpoint
    public MessageResponse resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        return authenticationService.resendOtp(request);
    }

    @PostMapping("/forgot-password")
    @PublicEndpoint
    public MessageResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return authenticationService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @PublicEndpoint
    public LoginResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return authenticationService.resetPassword(request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return authenticationService.changePassword(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        return authenticationService.logout(accessToken);
    }
}