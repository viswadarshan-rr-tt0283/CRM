package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DealWidgetDto {

    private long newDeals;
    private long qualifiedDeals;
    private long inProgressDeals;
    private long deliveredDeals;
    private long closedDeals;

    private BigDecimal totalPipelineAmount;
}
