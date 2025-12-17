package com.viswa.crm.dto.dashboard;

import com.viswa.crm.dto.activity.ActivityResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ActivityWidgetDto {

    private long pendingActivities;
    private long overdueActivities;
    private long completedActivities;

    private List<ActivityResponse> recentActivities;
}
