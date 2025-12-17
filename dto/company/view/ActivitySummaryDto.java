package com.viswa.crm.dto.company.view;

import com.viswa.crm.model.ActivityStatus;
import com.viswa.crm.model.ActivityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivitySummaryDto {

    private Long id;
    private ActivityType type;
    private String statusCode;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
