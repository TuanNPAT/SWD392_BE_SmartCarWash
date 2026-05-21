package com.swd392.smartcarwash.serviceImpl;

import com.swd392.smartcarwash.entity.EmailOtpToken;
import com.swd392.smartcarwash.enums.OtpPurpose;
import com.swd392.smartcarwash.exception.exceptions.BusinessException;
import com.swd392.smartcarwash.repository.EmailOtpTokenRepository;
import com.swd392.smartcarwash.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final EmailOtpTokenRepository emailOtpTokenRepository;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public EmailOtpToken generateOtp(String email, OtpPurpose purpose) {
        // Mark old OTPs with same email + purpose as used
        List<EmailOtpToken> oldTokens = emailOtpTokenRepository
                .findAllByEmailAndPurposeAndUsedFalse(email, purpose);
        for (EmailOtpToken oldToken : oldTokens) {
            oldToken.setUsed(true);
        }
        emailOtpTokenRepository.saveAll(oldTokens);

        // Generate 6-digit OTP
        String otpCode = generateRandomOtp();

        // Build and save new OTP token
        EmailOtpToken otpToken = EmailOtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();

        return emailOtpTokenRepository.save(otpToken);
    }

    @Override
    @Transactional
    public EmailOtpToken verifyOtp(String email, String otpCode, OtpPurpose purpose) {
        EmailOtpToken otpToken = emailOtpTokenRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP"));

        if (otpToken.isUsed()) {
            throw new BusinessException("OTP has already been used");
        }

        if (otpToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP has expired");
        }

        if (!otpToken.getOtpCode().equals(otpCode)) {
            throw new BusinessException("Invalid OTP code");
        }

        // Mark as used after successful verification
        otpToken.setUsed(true);
        emailOtpTokenRepository.save(otpToken);

        return otpToken;
    }

    private String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}
