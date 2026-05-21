package com.swd392.smartcarwash.service;

import com.swd392.smartcarwash.entity.EmailOtpToken;
import com.swd392.smartcarwash.enums.OtpPurpose;

public interface OtpService {

    /**
     * Generate a 6-digit OTP, mark old OTPs for the same email+purpose as used,
     * save and return the new OTP token.
     */
    EmailOtpToken generateOtp(String email, OtpPurpose purpose);

    /**
     * Verify OTP by email + purpose + otpCode.
     * Throws BusinessException if invalid, expired, or already used.
     * Sets used=true on success.
     */
    EmailOtpToken verifyOtp(String email, String otpCode, OtpPurpose purpose);
}
