package com.viswa.crm.security.strategy;

import com.viswa.crm.service.PermissionService;
import org.springframework.stereotype.Component;

@Component
public class PermissionBasedRoleStrategy implements RoleStrategy {

    private final PermissionService permissionService;

    public PermissionBasedRoleStrategy(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // ================= USER =================

    @Override
    public boolean canManageUsers(Long userId) {
        return permissionService.hasPermission(userId, "USER_MANAGE");
    }

    // ================= COMPANY =================

    @Override
    public boolean canCreateCompany(Long userId) {
        return permissionService.hasPermission(userId, "COMPANY_CREATE");
    }

    @Override
    public boolean canEditCompany(Long userId) {
        return permissionService.hasPermission(userId, "COMPANY_UPDATE");
    }

    @Override
    public boolean canDeleteCompany(Long userId) {
        return permissionService.hasPermission(userId, "COMPANY_DELETE");
    }

    // ================= CONTACT =================

    @Override
    public boolean canCreateContact(Long userId) {
        return permissionService.hasPermission(userId, "CONTACT_CREATE");
    }

    @Override
    public boolean canEditContact(Long userId) {
        return permissionService.hasPermission(userId, "CONTACT_UPDATE");
    }

    @Override
    public boolean canDeleteContact(Long userId) {
        return permissionService.hasPermission(userId, "CONTACT_DELETE");
    }

    // ================= DEAL =================

    @Override
    public boolean canCreateDeal(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_CREATE");
    }

    @Override
    public boolean canEditDeal(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_UPDATE");
    }

    @Override
    public boolean canDeleteDeal(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_DELETE");
    }

    @Override
    public boolean canViewAllDeals(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_VIEW_ALL");
    }

    @Deprecated(since = "2025-12-16", forRemoval = true)
    @Override
    public boolean canAssignDeal(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_REASSIGN");
    }

    @Override
    public boolean canChangeDealStatus(Long userId) {
        return permissionService.hasPermission(userId, "DEAL_CHANGE_STATUS");
    }

    // ================= ACTIVITY =================

    @Override
    public boolean canCreateActivity(Long userId) {
        return permissionService.hasPermission(userId, "ACTIVITY_CREATE");
    }

    @Override
    public boolean canEditActivity(Long userId) {
        return permissionService.hasPermission(userId, "ACTIVITY_UPDATE");
    }

    @Override
    public boolean canDeleteActivity(Long userId) {
        return permissionService.hasPermission(userId, "ACTIVITY_DELETE");
    }
}
