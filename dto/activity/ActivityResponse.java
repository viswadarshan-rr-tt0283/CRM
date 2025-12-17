package com.viswa.crm.dto.activity;

import com.viswa.crm.model.ActivityStatus;
import com.viswa.crm.model.ActivityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {

    private Long id;

    // Ownership
    private Long ownerUserId;

    // Deal context
    private Long dealId;
    private String dealTitle;

    // Activity nature
    private ActivityType type;
    private String statusCode;
    private String statusLabel;

    // Core data
    private String description;
    private LocalDate dueDate;

    // Audit
    private LocalDateTime createdAt;
}
