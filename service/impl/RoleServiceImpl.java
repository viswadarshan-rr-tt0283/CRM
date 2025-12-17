package com.viswa.crm.service.impl;

import com.viswa.crm.dto.role.PermissionResponse;
import com.viswa.crm.dto.role.CreateRoleRequest;
import com.viswa.crm.dto.role.RoleResponse;
import com.viswa.crm.model.Permission;
import com.viswa.crm.model.Role;
import com.viswa.crm.repository.PermissionRepository;
import com.viswa.crm.repository.RoleManagementRepository;
import com.viswa.crm.repository.UserRepository;
import com.viswa.crm.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleManagementRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {

        return permissionRepository.findAll()
                .stream()
                .map(p -> {
                    PermissionResponse r = new PermissionResponse();
                    r.setId(p.getId());
                    r.setPermissionCode(p.getPermissionCode());
                    r.setDescription(p.getDescription());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {

        Role role = new Role();
        role.setRoleName(request.getRoleName());

        Long roleId = roleRepository.save(role);
        roleRepository.addPermissions(roleId, request.getPermissionIds());

        return buildRoleResponse(roleId);
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(
            Long roleId,
            List<Long> permissionIds
    ) {

        roleRepository.deletePermissionsByRoleId(roleId);
        roleRepository.addPermissions(roleId, permissionIds);

        return buildRoleResponse(roleId);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(r -> buildRoleResponse(r.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {

        userRepository.updateRole(userId, roleId);
    }

    private RoleResponse buildRoleResponse(Long roleId) {

        Role role =
                roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Long> permissionIds =
                roleRepository.findPermissionIdsByRoleId(roleId);

        List<String> permissionCodes =
                permissionRepository.findByIds(permissionIds)
                        .stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.toList());

        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setPermissions(permissionCodes);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        return buildRoleResponse(roleId);
    }

}
