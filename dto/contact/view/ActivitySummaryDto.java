package com.viswa.crm.dto.contact.view;

import com.viswa.crm.model.ActivityStatus;
import com.viswa.crm.model.ActivityType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ActivitySummaryDto {

    private Long id;
    private ActivityType type;
    private String statusCode;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
