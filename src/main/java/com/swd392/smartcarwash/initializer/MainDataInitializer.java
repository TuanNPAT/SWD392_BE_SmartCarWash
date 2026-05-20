package com.swd392.smartcarwash.initializer;

import com.swd392.smartcarwash.initializer.components.DefaultUserInitializer;
import com.swd392.smartcarwash.initializer.components.PermissionRoleInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainDataInitializer implements CommandLineRunner {

    private final PermissionRoleInitializer permissionRoleInitializer;
    private final DefaultUserInitializer defaultUserInitializer;

    @Override
    public void run(String... args) {
        permissionRoleInitializer.init();
        defaultUserInitializer.init();
    }
}