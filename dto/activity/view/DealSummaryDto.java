package com.viswa.crm.dto.activity.view;

import com.viswa.crm.model.DealStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DealSummaryDto {

    private Long id;

    private String title;

    private BigDecimal amount;

    private DealStatus status;

    private Long companyId;
}
