package com.swd392.smartcarwash.controller;

import com.swd392.smartcarwash.annotation.PublicEndpoint;
import com.swd392.smartcarwash.annotation.SecuredEndpoint;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    @PublicEndpoint
    public String publicApi() {
        return "Public API is working";
    }

    @GetMapping("/auth")
    public String authApi() {
        return "JWT is working";
    }

    @GetMapping("/permission")
    @SecuredEndpoint("SYSTEM_REPORT_VIEW")
    public String permissionApi() {
        return "Permission SYSTEM_REPORT_VIEW is working";
    }
}