package com.swd392.smartcarwash.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;

public interface RolePermissionService {
    public Set<String> getPermissionsForRoles(Set<String> roleNames);
} 
