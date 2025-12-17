package com.viswa.crm.service;

import com.viswa.crm.dto.role.PermissionResponse;
import com.viswa.crm.dto.role.CreateRoleRequest;
import com.viswa.crm.dto.role.RoleResponse;

import java.util.List;

public interface RoleService {

    List<PermissionResponse> getAllPermissions();

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRolePermissions(Long roleId, List<Long> permissionIds);

    void deleteRole(Long roleId);

    List<RoleResponse> getAllRoles();

    void assignRoleToUser(Long userId, Long roleId);

    RoleResponse getRoleById(Long roleId);
}
