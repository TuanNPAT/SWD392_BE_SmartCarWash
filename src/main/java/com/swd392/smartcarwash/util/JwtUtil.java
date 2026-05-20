package com.swd392.smartcarwash.util;

import com.swd392.smartcarwash.entity.RefreshToken;
import com.swd392.smartcarwash.entity.User;
import com.swd392.smartcarwash.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        RefreshToken latestRefreshToken = refreshTokenRepository
                .findTopByUserAndIsRevokedFalseOrderByExpiryDateDesc(user)
                .orElse(null);

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("tokenVersion", user.getTokenVersion());
        claims.put("status", user.isLocked() ? "LOCKED" : user.getStatus().name());

        if (user.getRole() != null) {
            claims.put("role", user.getRole().getName());
        }

        claims.put("permissionCodes", user.getPermissionCodes());

        if (latestRefreshToken != null) {
            claims.put("jti", latestRefreshToken.getJti());
            claims.put("refreshExp", latestRefreshToken.getExpiryDate()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");

        if (userId instanceof Integer id) {
            return id.longValue();
        }

        if (userId instanceof Long id) {
            return id;
        }

        return Long.valueOf(userId.toString());
    }

    public Integer extractTokenVersion(String token) {
        Object version = extractAllClaims(token).get("tokenVersion");

        if (version instanceof Integer v) {
            return v;
        }

        return Integer.valueOf(version.toString());
    }

    public String extractJti(String token) {
        return extractAllClaims(token).get("jti", String.class);
    }

    public Set<String> extractPermissions(String token) {
        Object permissionsObj = extractAllClaims(token).get("permissionCodes");

        if (permissionsObj instanceof Collection<?> collection) {
            Set<String> permissions = new HashSet<>();

            for (Object item : collection) {
                permissions.add(String.valueOf(item));
            }

            return permissions;
        }

        return Collections.emptySet();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token, User user) {
        try {
            String username = extractUsername(token);
            Integer tokenVersion = extractTokenVersion(token);

            return username.equals(user.getUsername())
                    && tokenVersion.equals(user.getTokenVersion())
                    && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenWithJtiCheck(String token, User user) {
        try {
            String username = extractUsername(token);
            Integer tokenVersion = extractTokenVersion(token);
            String jti = extractJti(token);

            if (jti == null || jti.isBlank()) {
                return false;
            }

            boolean jtiExists = refreshTokenRepository.existsByJtiAndIsRevokedFalse(jti);

            return username.equals(user.getUsername())
                    && tokenVersion.equals(user.getTokenVersion())
                    && jtiExists
                    && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation with JTI failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenStructure(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT structure: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}