package com.viswa.crm.dto.auth.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DealSummaryDto {
    private Long id;
    private String title;
    private String status;
    private BigDecimal amount;
}
