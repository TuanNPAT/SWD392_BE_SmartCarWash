package com.swd392.smartcarwash.serviceImpl;

import com.swd392.smartcarwash.dto.request.auth.*;
import com.swd392.smartcarwash.dto.response.LoginResponse;
import com.swd392.smartcarwash.dto.response.MessageResponse;
import com.swd392.smartcarwash.entity.EmailOtpToken;
import com.swd392.smartcarwash.entity.RefreshToken;
import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.entity.User;
import com.swd392.smartcarwash.enums.AuthProvider;
import com.swd392.smartcarwash.enums.OtpPurpose;
import com.swd392.smartcarwash.enums.UserStatus;
import com.swd392.smartcarwash.exception.exceptions.BusinessException;
import com.swd392.smartcarwash.exception.exceptions.UnauthorizedException;
import com.swd392.smartcarwash.repository.RefreshTokenRepository;
import com.swd392.smartcarwash.repository.RoleRepository;
import com.swd392.smartcarwash.repository.UserRepository;
import com.swd392.smartcarwash.service.AuthenticationService;
import com.swd392.smartcarwash.service.MailService;
import com.swd392.smartcarwash.service.OtpService;
import com.swd392.smartcarwash.util.IpUtils;
import com.swd392.smartcarwash.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final MailService mailService;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    log.warn("CUSTOMER role not found, creating it automatically");
                    Role newRole = new Role();
                    newRole.setName("CUSTOMER");
                    return roleRepository.save(newRole);
                });

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

        // Generate and send VERIFY_EMAIL OTP
        // OTP save is independent — if mail fails, user + OTP still persist
        EmailOtpToken otpToken = otpService.generateOtp(request.getEmail(), OtpPurpose.VERIFY_EMAIL);
        mailService.sendVerificationOtp(request.getEmail(), otpToken.getOtpCode());

        return MessageResponse.builder()
                .message("Registration successful. Please check your email for the verification OTP.")
                .build();
    }

    @Override
    @Transactional
    public LoginResponse verifyEmail(VerifyOtpRequest request) {
        // Verify the OTP
        otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.VERIFY_EMAIL);

        // Set user as verified
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setVerify(true);
        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public MessageResponse resendOtp(ResendOtpRequest request) {
        // Only for existing unverified LOCAL users
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BusinessException("This account does not use local authentication");
        }

        if (user.isVerify()) {
            throw new BusinessException("Account is already verified");
        }

        // Generate and send VERIFY_EMAIL OTP
        EmailOtpToken otpToken = otpService.generateOtp(request.getEmail(), OtpPurpose.VERIFY_EMAIL);
        mailService.sendVerificationOtp(request.getEmail(), otpToken.getOtpCode());

        return MessageResponse.builder()
                .message("OTP has been resent to your email.")
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getProvider() == AuthProvider.GOOGLE && user.getPassword() == null) {
            throw new BusinessException("This account uses Google login. Please use Google Sign-In.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isVerify()) {
            throw new BusinessException("Account is not verified. Please verify your email first.");
        }

        if (user.isLocked()) {
            throw new BusinessException("Account is locked. Please contact support.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // If email exists and provider=LOCAL, send FORGOT_PASSWORD OTP
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (user.getProvider() == AuthProvider.LOCAL) {
                EmailOtpToken otpToken = otpService.generateOtp(request.getEmail(), OtpPurpose.FORGOT_PASSWORD);
                mailService.sendPasswordResetOtp(request.getEmail(), otpToken.getOtpCode());
            }
        });

        // Always return generic message to prevent email enumeration
        return MessageResponse.builder()
                .message("If your email is registered, you will receive a password reset OTP.")
                .build();
    }

    @Override
    @Transactional
    public LoginResponse resetPassword(ResetPasswordRequest request) {
        // Verify FORGOT_PASSWORD OTP
        otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.FORGOT_PASSWORD);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        // Encode new password and increment token version
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.incrementTokenVersion();
        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        User currentUser = (User) authentication.getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException("User not found"));

        // Check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }

        // Encode new password and increment token version
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.incrementTokenVersion();
        userRepository.save(user);

        return MessageResponse.builder()
                .message("Password changed successfully.")
                .build();
    }

    @Override
    public MessageResponse logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return MessageResponse.builder()
                    .message("Logged out successfully.")
                    .build();
        }

        try {
            String jti = jwtUtil.extractJti(accessToken);

            if (jti != null && !jti.isBlank()) {
                refreshTokenRepository.findByJti(jti).ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshToken.setRevokedAt(LocalDateTime.now());
                    refreshToken.setRevokedReason("User logout");
                    refreshTokenRepository.save(refreshToken);
                });
            }
        } catch (Exception e) {
            log.warn("Failed to revoke refresh token during logout: {}", e.getMessage());
        }

        return MessageResponse.builder()
                .message("Logged out successfully.")
                .build();
    }

    @Override
    public LoginResponse loginWithGoogle(String email, String fullName, String avatarUrl, String providerId) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("CUSTOMER");
                        return roleRepository.save(newRole);
                    });

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