package com.swd392.smartcarwash.controller;

import com.swd392.smartcarwash.annotation.PublicEndpoint;
import com.swd392.smartcarwash.dto.request.auth.LoginRequest;
import com.swd392.smartcarwash.dto.request.auth.RegisterRequest;
import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.service.AuthenticationService;
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
    public LoginResponse register(
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
}