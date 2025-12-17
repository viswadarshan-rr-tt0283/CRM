package com.viswa.crm.repository;

import com.viswa.crm.model.Permission;

import java.util.List;

public interface PermissionRepository {

    boolean hasPermission(Long userId, String permissionCode);

    List<Permission> findAll();

    List<Permission> findByIds(List<Long> ids);

    boolean existsByCode(String permissionCode);
}
