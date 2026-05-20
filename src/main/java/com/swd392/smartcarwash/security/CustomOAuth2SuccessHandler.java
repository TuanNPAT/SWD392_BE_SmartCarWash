package com.swd392.smartcarwash.security;

import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Value("${frontend.base-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getAttribute("sub");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=missing_email");
            return;
        }

        try {
            LoginResponse loginResponse = authenticationService.loginWithGoogle(
                    email,
                    fullName,
                    avatarUrl,
                    providerId
            );

            String redirectUrl = frontendUrl
                    + "/oauth2/success"
                    + "?token=" + URLEncoder.encode(loginResponse.getToken(), StandardCharsets.UTF_8)
                    + "&refreshToken=" + URLEncoder.encode(loginResponse.getRefreshToken(), StandardCharsets.UTF_8);

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(
                    e.getMessage(),
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(frontendUrl + "/login?error=" + errorMessage);
        }
    }
}