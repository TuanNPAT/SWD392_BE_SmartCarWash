package com.swd392.smartcarwash.initializer.components;

import com.swd392.smartcarwash.entity.Permission;
import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.repository.PermissionRepository;
import com.swd392.smartcarwash.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionRoleInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static final String CUSTOMER = "CUSTOMER";
    private static final String STAFF = "STAFF";
    private static final String MANAGER = "MANAGER";
    private static final String ADMIN = "ADMIN";

    private static final String SYSTEM_REPORT_VIEW = "SYSTEM_REPORT_VIEW";
    private static final String USER_MANAGE = "USER_MANAGE";

    private static final String BOOKING_VIEW = "BOOKING_VIEW";
    private static final String BOOKING_CREATE = "BOOKING_CREATE";
    private static final String BOOKING_UPDATE = "BOOKING_UPDATE";
    private static final String BOOKING_CANCEL = "BOOKING_CANCEL";
    private static final String BOOKING_CHECK_IN = "BOOKING_CHECK_IN";
    private static final String BOOKING_COMPLETE = "BOOKING_COMPLETE";

    private static final String SERVICE_VIEW = "SERVICE_VIEW";
    private static final String SERVICE_MANAGE = "SERVICE_MANAGE";

    private static final String VEHICLE_VIEW = "VEHICLE_VIEW";
    private static final String VEHICLE_MANAGE = "VEHICLE_MANAGE";

    private static final String LOYALTY_VIEW = "LOYALTY_VIEW";
    private static final String LOYALTY_MANAGE = "LOYALTY_MANAGE";

    private static final String PAYMENT_VIEW = "PAYMENT_VIEW";
    private static final String PAYMENT_MANAGE = "PAYMENT_MANAGE";

    @Transactional
    public void init() {
        log.info("Initializing permissions and roles...");

        createPermissions();
        createRoles();

        log.info("Initialized {} permissions and {} roles",
                permissionRepository.count(),
                roleRepository.count());
    }

    private void createPermissions() {
        ensurePermission(SYSTEM_REPORT_VIEW, "View system reports");
        ensurePermission(USER_MANAGE, "Manage users");

        ensurePermission(BOOKING_VIEW, "View bookings");
        ensurePermission(BOOKING_CREATE, "Create bookings");
        ensurePermission(BOOKING_UPDATE, "Update bookings");
        ensurePermission(BOOKING_CANCEL, "Cancel bookings");
        ensurePermission(BOOKING_CHECK_IN, "Check-in booking");
        ensurePermission(BOOKING_COMPLETE, "Complete booking");

        ensurePermission(SERVICE_VIEW, "View car wash services");
        ensurePermission(SERVICE_MANAGE, "Manage car wash services");

        ensurePermission(VEHICLE_VIEW, "View vehicles");
        ensurePermission(VEHICLE_MANAGE, "Manage vehicles");

        ensurePermission(LOYALTY_VIEW, "View loyalty points");
        ensurePermission(LOYALTY_MANAGE, "Manage loyalty program");

        ensurePermission(PAYMENT_VIEW, "View payments");
        ensurePermission(PAYMENT_MANAGE, "Manage payments");
    }

    private void createRoles() {
        Map<String, Permission> permMap = permissionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getCode, permission -> permission));

        ensureRole(CUSTOMER, Set.of(
                permMap.get(BOOKING_VIEW),
                permMap.get(BOOKING_CREATE),
                permMap.get(BOOKING_CANCEL),
                permMap.get(SERVICE_VIEW),
                permMap.get(VEHICLE_VIEW),
                permMap.get(VEHICLE_MANAGE),
                permMap.get(LOYALTY_VIEW),
                permMap.get(PAYMENT_VIEW)
        ));

        ensureRole(STAFF, Set.of(
                permMap.get(BOOKING_VIEW),
                permMap.get(BOOKING_UPDATE),
                permMap.get(BOOKING_CHECK_IN),
                permMap.get(BOOKING_COMPLETE),
                permMap.get(SERVICE_VIEW),
                permMap.get(PAYMENT_VIEW)
        ));

        ensureRole(MANAGER, Set.of(
                permMap.get(SYSTEM_REPORT_VIEW),
                permMap.get(BOOKING_VIEW),
                permMap.get(BOOKING_UPDATE),
                permMap.get(SERVICE_VIEW),
                permMap.get(SERVICE_MANAGE),
                permMap.get(LOYALTY_VIEW),
                permMap.get(LOYALTY_MANAGE),
                permMap.get(PAYMENT_VIEW)
        ));

        ensureRole(ADMIN, new HashSet<>(permMap.values()));
    }

    private void ensurePermission(String code, String name) {
        if (permissionRepository.findByCode(code).isPresent()) {
            return;
        }

        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);

        permissionRepository.save(permission);
    }

    private void ensureRole(String roleName, Set<Permission> permissions) {
        Set<Permission> validPermissions = permissions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Optional<Role> existingRole = roleRepository.findByName(roleName);

        if (existingRole.isPresent()) {
            Role role = existingRole.get();

            Set<Permission> currentPermissions = role.getPermissions() == null
                    ? new HashSet<>()
                    : new HashSet<>(role.getPermissions());

            boolean changed = currentPermissions.addAll(validPermissions);

            if (changed) {
                role.setPermissions(currentPermissions);
                roleRepository.save(role);
            }

            return;
        }

        Role role = new Role();
        role.setName(roleName);
        role.setPermissions(validPermissions);

        roleRepository.save(role);
    }
}