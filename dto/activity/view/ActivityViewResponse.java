package com.viswa.crm.dto.activity.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActivityViewResponse {

    private ActivityDetailDto activity;

    private DealSummaryDto deal;

    private CompanySummaryDto company;

    private List<ContactSummaryDto> contacts;

    private ActivityStatusOptionsDto statusOptions;
}
