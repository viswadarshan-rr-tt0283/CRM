package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RecentSectionDto {

    private List<RecentItemDto> recentDeals;
    private List<RecentItemDto> recentContacts;
    private List<RecentItemDto> recentActivities;
}
