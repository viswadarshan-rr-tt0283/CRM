package com.viswa.crm.service.impl;

import com.viswa.crm.repository.PermissionRepository;
import com.viswa.crm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        return permissionRepository.hasPermission(userId, permissionCode);
    }
}
