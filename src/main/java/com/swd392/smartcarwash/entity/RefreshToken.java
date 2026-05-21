package com.swd392.smartcarwash.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    @ToString.Include
    private String token;

    @Column(unique = true)
    @ToString.Include
    private String jti;

    @Column(nullable = false)
    @ToString.Include
    private LocalDateTime expiryDate;

    @Column(length = 100)
    @ToString.Include
    private String deviceId;

    @Column(length = 150)
    @ToString.Include
    private String deviceName;

    @Column(length = 45)
    @ToString.Include
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    private LocalDateTime issuedAt;

    private LocalDateTime revokedAt;

    @Column(length = 255)
    private String revokedReason;

    @ManyToOne
    @JoinColumn(name = "replaced_by_token_id")
    private RefreshToken replacedByToken;

    @Column(nullable = false)
    @ToString.Include
    private boolean isRevoked = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
