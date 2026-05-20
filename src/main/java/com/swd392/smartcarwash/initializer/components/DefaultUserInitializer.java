package com.swd392.smartcarwash.initializer.components;

import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.entity.User;
import com.swd392.smartcarwash.enums.AuthProvider;
import com.swd392.smartcarwash.enums.UserStatus;
import com.swd392.smartcarwash.repository.RoleRepository;
import com.swd392.smartcarwash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultUserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEFAULT_ADMIN_EMAIL:admin@smartcarwash.com}")
    private String defaultAdminEmail;

    @Value("${DEFAULT_ADMIN_PASSWORD:Admin@123456}")
    private String defaultAdminPassword;

    public void init() {
        if (userRepository.existsByEmail(defaultAdminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User admin = User.builder()
                .username(defaultAdminEmail)
                .email(defaultAdminEmail)
                .password(passwordEncoder.encode(defaultAdminPassword))
                .fullName("System Admin")
                .role(adminRole)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .verify(true)
                .locked(false)
                .tokenVersion(0)
                .build();

        userRepository.save(admin);
    }
}