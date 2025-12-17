package com.viswa.crm.service;

import com.viswa.crm.dto.dashboard.DashboardResponse;
import com.viswa.crm.dto.dashboard.DashboardSummaryResponse;
import com.viswa.crm.dto.dashboard.RecentItem;

import java.util.List;

public interface DashboardService {

    DashboardResponse loadDashboard(
            Long userId,
            String roleName,
            int recentLimit
    );
}
