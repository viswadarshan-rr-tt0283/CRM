package com.viswa.crm.dto.contact.view;

import com.viswa.crm.dto.common.TimelineItemDto;
import com.viswa.crm.dto.contact.ContactResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ContactDetailResponse {

    private ContactResponse contact;

    private CompanySummaryDto company;

    private List<DealSummaryDto> deals;

    private List<ActivitySummaryDto> activities;

    private List<TimelineItemDto> timeline;
}
