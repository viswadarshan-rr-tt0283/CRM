package com.viswa.crm.dto.company.view;

import com.viswa.crm.model.DealStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DealSummaryDto {

    private Long id;
    private String title;
    private BigDecimal amount;
    private DealStatus status;

    private String assignedUserName;
    private String contactName;

    private LocalDateTime createdAt;
}
