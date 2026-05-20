package com.swd392.smartcarwash.service;



import com.swd392.smartcarwash.dto.request.role.CreateRoleRequest;
import com.swd392.smartcarwash.dto.request.role.RolePermissionsRequest;
import com.swd392.smartcarwash.dto.request.role.UpdateRoleRequest;
import com.swd392.smartcarwash.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAll();

    RoleResponse create(CreateRoleRequest req);

    RoleResponse update(Long id, UpdateRoleRequest req);

    void delete(Long id);

    RoleResponse updatePermissions(Long id, RolePermissionsRequest req);

    RoleResponse getById(Long id);
}
