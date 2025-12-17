package com.viswa.crm.security.strategy;

public interface RoleStrategy {

    // User
    boolean canManageUsers(Long userId);

    // Company
    boolean canCreateCompany(Long userId);
    boolean canEditCompany(Long userId);
    boolean canDeleteCompany(Long userId);

    // Contact
    boolean canCreateContact(Long userId);
    boolean canEditContact(Long userId);
    boolean canDeleteContact(Long userId);

    // Deal
    boolean canCreateDeal(Long userId);
    boolean canEditDeal(Long userId);
    boolean canDeleteDeal(Long userId);
    boolean canViewAllDeals(Long userId);
    boolean canAssignDeal(Long userId);
    boolean canChangeDealStatus(Long userId);

    // Activity
    boolean canCreateActivity(Long userId);
    boolean canEditActivity(Long userId);
    boolean canDeleteActivity(Long userId);
}
