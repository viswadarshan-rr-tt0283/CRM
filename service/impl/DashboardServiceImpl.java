package com.viswa.crm.service.impl;

import com.viswa.crm.dto.activity.ActivityResponse;
import com.viswa.crm.dto.dashboard.*;
import com.viswa.crm.model.Activity;
import com.viswa.crm.model.DealStatus;
import com.viswa.crm.model.User;
import com.viswa.crm.repository.DashboardRepository;
import com.viswa.crm.repository.RecentProjection;
import com.viswa.crm.repository.UserRepository;
import com.viswa.crm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.viswa.crm.dto.dashboard.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse loadDashboard(
            Long userId,
            String roleName,
            int recentLimit
    ) {

        return DashboardResponse.builder()
                .userContext(buildUserContext(userId, roleName))
                .summary(buildSummaryWidget(userId, roleName))
                .dealWidget(buildDealWidget(userId, roleName))
                .activityWidget(buildActivityWidget(userId, roleName, recentLimit))
                .recent(buildRecentSection(userId, roleName, recentLimit))
                .build();
    }

    /* =========================================================
       USER CONTEXT
       ========================================================= */
    private UserContextDto buildUserContext(Long userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserContextDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(roleName)
                .build();
    }


    /* =========================================================
       SUMMARY WIDGET
       ========================================================= */

    private SummaryWidgetDto buildSummaryWidget(Long userId, String role) {

        SummaryWidgetDto.SummaryWidgetDtoBuilder builder =
                SummaryWidgetDto.builder();

        if ("ADMIN".equals(role)) {
            builder.totalCompanies(dashboardRepository.countCompanies())
                    .totalContacts(dashboardRepository.countContacts())
                    .totalUsers(dashboardRepository.countUsers())
                    .totalDeals(dashboardRepository.countDealsForAdmin())
                    .pendingActivities(
                            dashboardRepository.countPendingActivitiesForAdmin()
                    );
        } else if ("MANAGER".equals(role)) {
            builder.totalDeals(
                            dashboardRepository.countDealsForManager(userId)
                    )
                    .pendingActivities(
                            dashboardRepository.countPendingActivitiesForManager(userId)
                    );
        } else {
            builder.totalDeals(
                            dashboardRepository.countDealsForSales(userId)
                    )
                    .pendingActivities(
                            dashboardRepository.countPendingActivitiesForSales(userId)
                    );
        }

        return builder.build();
    }

    /* =========================================================
       DEAL WIDGET
       ========================================================= */

    private DealWidgetDto buildDealWidget(Long userId, String role) {

        Map<DealStatus, Long> statusCounts;

        if ("ADMIN".equals(role)) {
            statusCounts =
                    dashboardRepository.countDealsByStatusForAdmin();
        } else if ("MANAGER".equals(role)) {
            statusCounts =
                    dashboardRepository.countDealsByStatusForManager(userId);
        } else {
            statusCounts =
                    dashboardRepository.countDealsByStatusForSales(userId);
        }

        return DealWidgetDto.builder()
                .newDeals(statusCounts.getOrDefault(DealStatus.NEW, 0L))
                .qualifiedDeals(statusCounts.getOrDefault(DealStatus.QUALIFIED, 0L))
                .inProgressDeals(statusCounts.getOrDefault(DealStatus.IN_PROGRESS, 0L))
                .deliveredDeals(statusCounts.getOrDefault(DealStatus.DELIVERED, 0L))
                .closedDeals(statusCounts.getOrDefault(DealStatus.CLOSED, 0L))
                .totalPipelineAmount(resolvePipelineAmount(userId, role))
                .build();
    }

    private java.math.BigDecimal resolvePipelineAmount(Long userId, String role) {
        if ("ADMIN".equals(role)) {
            return dashboardRepository.sumPipelineAmountForAdmin();
        }
        if ("MANAGER".equals(role)) {
            return dashboardRepository.sumPipelineAmountForManager(userId);
        }
        return dashboardRepository.sumPipelineAmountForSales(userId);
    }

    /* =========================================================
       ACTIVITY WIDGET
       ========================================================= */

    private ActivityWidgetDto buildActivityWidget(
            Long userId,
            String role,
            int limit
    ) {

        long pending;
        long overdue;
        long completed;

        List<Activity> recentActivities;

        if ("ADMIN".equals(role)) {
            pending = dashboardRepository.countPendingActivitiesForAdmin();
            overdue = dashboardRepository.countOverdueActivitiesForAdmin();
            completed = dashboardRepository.countCompletedActivitiesForAdmin();
            recentActivities =
                    dashboardRepository.findRecentActivitiesForAdmin(limit);

        } else if ("MANAGER".equals(role)) {
            pending =
                    dashboardRepository.countPendingActivitiesForManager(userId);
            overdue =
                    dashboardRepository.countOverdueActivitiesForManager(userId);
            completed =
                    dashboardRepository.countCompletedActivitiesForManager(userId);
            recentActivities =
                    dashboardRepository.findRecentActivitiesForManager(userId, limit);

        } else {
            pending =
                    dashboardRepository.countPendingActivitiesForSales(userId);
            overdue =
                    dashboardRepository.countOverdueActivitiesForSales(userId);
            completed =
                    dashboardRepository.countCompletedActivitiesForSales(userId);
            recentActivities =
                    dashboardRepository.findRecentActivitiesForSales(userId, limit);
        }

        return ActivityWidgetDto.builder()
                .pendingActivities(pending)
                .overdueActivities(overdue)
                .completedActivities(completed)
                .recentActivities(
                        recentActivities.stream()
                                .map(this::mapActivity)
                                .collect(Collectors.toList())
                )
                .build();
    }

    /* =========================================================
       RECENT SECTION
       ========================================================= */

    private RecentSectionDto buildRecentSection(
            Long userId,
            String role,
            int limit
    ) {

        List<RecentItemDto> deals;
        List<Activity> activities;

        if ("ADMIN".equals(role)) {
            deals = mapRecentDeals(
                    dashboardRepository.findRecentDealsForAdmin(limit)
            );
            activities =
                    dashboardRepository.findRecentActivitiesForAdmin(limit);

        } else if ("MANAGER".equals(role)) {
            deals = mapRecentDeals(
                    dashboardRepository.findRecentDealsForManager(userId, limit)
            );
            activities =
                    dashboardRepository.findRecentActivitiesForManager(userId, limit);

        } else {
            deals = mapRecentDeals(
                    dashboardRepository.findRecentDealsForSales(userId, limit)
            );
            activities =
                    dashboardRepository.findRecentActivitiesForSales(userId, limit);
        }

        return RecentSectionDto.builder()
                .recentDeals(deals)
                .recentContacts(List.of())
                .recentActivities(
                        activities.stream()
                                .map(this::mapActivityToRecent)
                                .collect(Collectors.toList())
                )
                .build();
    }

    /* =========================================================
       MAPPERS
       ========================================================= */

    private List<RecentItemDto> mapRecentDeals(List<RecentProjection> projections) {
        return projections.stream()
                .map(p ->
                        RecentItemDto.builder()
                                .id(p.getId())
                                .title(p.getTitle())
                                .type("DEAL")
                                .createdAt(p.getCreatedAt())
                                .build()
                )
                .collect(Collectors.toList());
    }

    private RecentItemDto mapActivityToRecent(Activity a) {
        return RecentItemDto.builder()
                .id(a.getId())
                .title(a.getActivityType() + ": " + a.getDescription())
                .type("ACTIVITY")
                .createdAt(a.getCreatedAt())
                .build();
    }

    private ActivityResponse mapActivity(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .dealId(a.getDeal().getId())
                .type(a.getActivityType())
                .statusCode(a.getStatusCode())
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
