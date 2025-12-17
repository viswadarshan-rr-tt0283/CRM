package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SummaryWidgetDto {

    // Visible to all (scoped)
    private long totalDeals;
    private long pendingActivities;

    // Role-specific visibility handled in service
    private Long totalCompanies;
    private Long totalContacts;
    private Long totalUsers;
}
