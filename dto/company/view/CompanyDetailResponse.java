package com.viswa.crm.dto.company.view;

import com.viswa.crm.dto.common.TimelineItemDto;
//import com.viswa.crm.dto.company.view.ActivitySummaryDto;
//import com.viswa.crm.dto.company.view.DealSummaryDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyDetailResponse {

    private CompanyHeaderDto company;

    private List<ContactSummaryDto> contacts;

    private List<DealSummaryDto> deals;

    private List<ActivitySummaryDto> activities;

    private List<TimelineItemDto> timeline;
}
