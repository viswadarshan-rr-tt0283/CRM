package com.viswa.crm.service;

public interface PermissionService {
    boolean hasPermission(Long userId, String permissionCode);
}
