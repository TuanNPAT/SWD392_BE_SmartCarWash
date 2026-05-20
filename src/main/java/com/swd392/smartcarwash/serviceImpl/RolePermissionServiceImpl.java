package com.swd392.smartcarwash.serviceImpl;

import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.repository.RoleRepository;
import com.swd392.smartcarwash.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;

    @Override
    public Set<String> getPermissionsForRoles(Set<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Role> roles = new HashSet<>(
                roleRepository.findAllByNameIn(roleNames)
        );

        return roles.stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .collect(Collectors.toSet());
    }
}