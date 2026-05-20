package com.swd392.smartcarwash.repository;

import com.swd392.smartcarwash.entity.RefreshToken;
import com.swd392.smartcarwash.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findTopByUserAndIsRevokedFalseOrderByExpiryDateDesc(User user);

    boolean existsByJtiAndIsRevokedFalse(String jti);

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByJti(String jti);
}