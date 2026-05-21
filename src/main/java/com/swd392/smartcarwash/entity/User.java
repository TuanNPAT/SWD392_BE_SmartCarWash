package com.swd392.smartcarwash.entity;

import com.swd392.smartcarwash.enums.AuthProvider;
import com.swd392.smartcarwash.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    // Local login dùng username, có thể set = email
    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String username;

    // Google account có thể không có password
    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    @ToString.Include
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    @ToString.Include
    private String fullName;

    @Column(name = "phone_number", length = 20)
    @ToString.Include
    private String phoneNumber;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private AuthProvider provider;

    // Google subject id
    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Builder.Default
    @Column(name = "is_verify", nullable = false)
    @ToString.Include
    private boolean verify = false;

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    @ToString.Include
    private boolean locked = false;

    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public Set<String> getPermissionCodes() {
        if (role == null || role.getPermissions() == null) {
            return Collections.emptySet();
        }

        return role.getPermissions()
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()))
                );
            }
        }

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && verify;
    }
}