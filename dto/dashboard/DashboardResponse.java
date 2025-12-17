package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardResponse {

    private UserContextDto userContext;

    private SummaryWidgetDto summary;

    private DealWidgetDto dealWidget;

    private ActivityWidgetDto activityWidget;

    private RecentSectionDto recent;
}
