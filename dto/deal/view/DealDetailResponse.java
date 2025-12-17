package com.viswa.crm.dto.deal.view;

import com.viswa.crm.dto.company.view.ContactSummaryDto;
import com.viswa.crm.dto.contact.view.CompanySummaryDto;
import com.viswa.crm.dto.deal.DealResponse;
import com.viswa.crm.model.DealStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DealDetailResponse {

    // Core deal info
    private DealResponse deal;

    // Related entities (summaries only)
    private CompanySummaryDto company;
    private ContactSummaryDto contact;

    // Timeline
    private List<ActivitySummaryDto> activities;

    // Workflow support
    private List<DealStatus> allowedNextStatuses;
}
