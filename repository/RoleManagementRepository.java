package com.viswa.crm.repository;

import com.viswa.crm.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleManagementRepository {

    Long save(Role role);

    Optional<Role> findById(Long roleId);

    List<Role> findAll();

    void deleteById(Long roleId);

    void addPermissions(Long roleId, List<Long> permissionIds);

    void deletePermissionsByRoleId(Long roleId);

    List<Long> findPermissionIdsByRoleId(Long roleId);
}
