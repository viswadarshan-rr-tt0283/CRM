package com.viswa.crm.dto.contact.view;

import com.viswa.crm.model.DealStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DealSummaryDto {

    private Long id;
    private String title;
    private BigDecimal amount;
    private DealStatus status;
    private LocalDateTime createdAt;
}

