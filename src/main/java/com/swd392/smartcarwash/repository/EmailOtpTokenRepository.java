package com.swd392.smartcarwash.repository;

import com.swd392.smartcarwash.entity.EmailOtpToken;
import com.swd392.smartcarwash.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailOtpTokenRepository extends JpaRepository<EmailOtpToken, Long> {
    Optional<EmailOtpToken> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);
    List<EmailOtpToken> findAllByEmailAndPurposeAndUsedFalse(String email, OtpPurpose purpose);
    void deleteAllByEmailAndPurpose(String email, OtpPurpose purpose);
}
