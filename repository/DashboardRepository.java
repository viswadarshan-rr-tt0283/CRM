package com.viswa.crm.repository;

import com.viswa.crm.model.Activity;
import com.viswa.crm.model.DealStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardRepository {

    /* =============================
       SUMMARY COUNTS
       ============================= */

    long countDealsForAdmin();
    long countDealsForManager(Long managerId);
    long countDealsForSales(Long userId);

    long countCompanies();        // admin only
    long countContacts();         // admin only
    long countUsers();            // admin only

    long countPendingActivitiesForAdmin();
    long countPendingActivitiesForManager(Long managerId);
    long countPendingActivitiesForSales(Long userId);

    /* =============================
       DEAL PIPELINE
       ============================= */

    Map<DealStatus, Long> countDealsByStatusForAdmin();
    Map<DealStatus, Long> countDealsByStatusForManager(Long managerId);
    Map<DealStatus, Long> countDealsByStatusForSales(Long userId);

    BigDecimal sumPipelineAmountForAdmin();
    BigDecimal sumPipelineAmountForManager(Long managerId);
    BigDecimal sumPipelineAmountForSales(Long userId);

    /* =============================
       ACTIVITIES
       ============================= */

    long countOverdueActivitiesForAdmin();
    long countOverdueActivitiesForManager(Long managerId);
    long countOverdueActivitiesForSales(Long userId);

    long countCompletedActivitiesForAdmin();
    long countCompletedActivitiesForManager(Long managerId);
    long countCompletedActivitiesForSales(Long userId);

    List<Activity> findRecentActivitiesForAdmin(int limit);
    List<Activity> findRecentActivitiesForManager(Long managerId, int limit);
    List<Activity> findRecentActivitiesForSales(Long userId, int limit);

    /* =============================
       RECENT ITEMS
       ============================= */

    List<RecentProjection> findRecentDealsForAdmin(int limit);
    List<RecentProjection> findRecentDealsForManager(Long managerId, int limit);
    List<RecentProjection> findRecentDealsForSales(Long userId, int limit);

    List<RecentProjection> findRecentContacts(int limit); // global is fine
}
