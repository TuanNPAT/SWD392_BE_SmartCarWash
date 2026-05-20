package com.swd392.smartcarwash.serviceImpl;

import com.swd392.smartcarwash.dto.request.auth.LoginRequest;
import com.swd392.smartcarwash.dto.request.auth.RegisterRequest;
import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.entity.RefreshToken;
import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.entity.User;
import com.swd392.smartcarwash.enums.AuthProvider;
import com.swd392.smartcarwash.enums.UserStatus;
import com.swd392.smartcarwash.exception.exceptions.BusinessException;
import com.swd392.smartcarwash.exception.exceptions.UnauthorizedException;
import com.swd392.smartcarwash.repository.RefreshTokenRepository;
import com.swd392.smartcarwash.repository.RoleRepository;
import com.swd392.smartcarwash.repository.UserRepository;
import com.swd392.smartcarwash.service.AuthenticationService;
import com.swd392.smartcarwash.util.IpUtils;
import com.swd392.smartcarwash.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpirationMs;

    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new BusinessException("Default role CUSTOMER not found"));

        User user = User.builder()
                .username(request.getEmail())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(customerRole)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .verify(false)
                .locked(false)
                .tokenVersion(0)
                .build();

        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getProvider() == AuthProvider.GOOGLE && user.getPassword() == null) {
            throw new BusinessException("This account uses Google login");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.isLocked()) {
            throw new UnauthorizedException("Account is locked");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is not active or not verified");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse loginWithGoogle(String email, String fullName, String avatarUrl, String providerId) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new BusinessException("Default role CUSTOMER not found"));

            return User.builder()
                    .username(email)
                    .email(email)
                    .password(null)
                    .fullName(fullName != null ? fullName : email)
                    .avatarUrl(avatarUrl)
                    .providerId(providerId)
                    .provider(AuthProvider.GOOGLE)
                    .role(customerRole)
                    .status(UserStatus.ACTIVE)
                    .verify(true)
                    .locked(false)
                    .tokenVersion(0)
                    .build();
        });

        user.setFullName(fullName != null ? fullName : user.getFullName());
        user.setAvatarUrl(avatarUrl);
        user.setProviderId(providerId);
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public void logout(String accessToken) {
        String jti = jwtUtil.extractJti(accessToken);

        if (jti == null || jti.isBlank()) {
            return;
        }

        refreshTokenRepository.findByJti(jti).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshToken.setRevokedReason("User logout");
            refreshTokenRepository.save(refreshToken);
        });
    }


    private LoginResponse buildLoginResponse(User user) {
        RefreshToken refreshToken = createRefreshToken(user);
        String accessToken = jwtUtil.generateToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setJti(UUID.randomUUID().toString());
        refreshToken.setIssuedAt(LocalDateTime.now());
        refreshToken.setExpiryDate(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshToken.setRevoked(false);
        refreshToken.setIpAddress(IpUtils.getClientIpAddress());
        refreshToken.setUserAgent(IpUtils.getUserAgent());
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setUpdatedAt(LocalDateTime.now());

        return refreshTokenRepository.save(refreshToken);
    }
}