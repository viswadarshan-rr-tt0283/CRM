package com.viswa.crm.repository;

import com.viswa.crm.model.Activity;
import com.viswa.crm.model.ActivityStatus;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository {

    Optional<Activity> findById(Long id);

    List<Activity> findByDealId(Long dealId);

    List<Activity> findRecentByDealOwner(Long userId, int limit);
    // NEW: ownership-aware
    List<Activity> findByOwner(Long ownerUserId);

    List<Activity> findRecentActivities(int limit);

    // NEW: role-based recent fetch
    List<Activity> findRecentActivitiesByOwner(Long ownerUserId, int limit);

    Long save(Activity activity);

    void update(Activity activity);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByDealId(Long dealId);

    List<Activity> findByManagerId(Long managerId);

    List<Activity> findByContactId(Long contactId);

    List<Activity> findByCompanyId(Long companyId);

    List<Activity> findRecentByDealIds(List<Long> dealIds, int limit);

}
