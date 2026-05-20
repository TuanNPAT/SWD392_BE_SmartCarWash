package com.swd392.smartcarwash.serviceImpl;

import com.swd392.smartcarwash.dto.request.role.CreateRoleRequest;
import com.swd392.smartcarwash.dto.request.role.RolePermissionsRequest;
import com.swd392.smartcarwash.dto.request.role.UpdateRoleRequest;
import com.swd392.smartcarwash.dto.response.RoleResponse;
import com.swd392.smartcarwash.entity.Role;
import com.swd392.smartcarwash.exception.exceptions.BusinessException;
import com.swd392.smartcarwash.exception.exceptions.NotFoundException;
import com.swd392.smartcarwash.mapper.RoleMapper;
import com.swd392.smartcarwash.repository.RoleRepository;
import com.swd392.smartcarwash.repository.UserRepository;
import com.swd392.smartcarwash.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;

    @Override
    public List<RoleResponse> getAll() {

        List<Role> roles = roleRepository.findAll();

        if (roles.isEmpty()) {
            throw new NotFoundException("No roles found");
        }

        return roleMapper.toResponseList(roles);
    }

    @Override
    public RoleResponse getById(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role with id " + id + " not found"));

        return roleMapper.toResponse(role);
    }

    @Override
    public RoleResponse create(CreateRoleRequest req) {

        if (roleRepository.existsByName(req.getName())) {
            throw new BusinessException("Role name already exists");
        }

        Role role = new Role();
        roleMapper.fromCreateDto(req, role);

        Role savedRole = roleRepository.save(role);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    public RoleResponse update(Long id, UpdateRoleRequest req) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role with id " + id + " not found"));

        roleMapper.fromUpdateDto(req, role);

        Role updatedRole = roleRepository.save(role);

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    public void delete(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role with id " + id + " not found"));

        if (userRepository.existsByRole_Id(id)) {
            throw new BusinessException("Role is assigned to users. Cannot delete.");
        }

        roleRepository.delete(role);
    }

    @Override
    public RoleResponse updatePermissions(Long id, RolePermissionsRequest req) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role with id " + id + " not found"));

        roleMapper.fromPermissionDto(req, role);

        Role updatedRole = roleRepository.save(role);

        return roleMapper.toResponse(updatedRole);
    }
}