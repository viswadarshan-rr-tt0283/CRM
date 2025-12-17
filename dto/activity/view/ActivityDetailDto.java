package com.viswa.crm.dto.activity.view;

import com.viswa.crm.model.ActivityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityDetailDto {

    private Long id;

    private ActivityType type;

    private String statusCode;

    private String statusLabel;

    private String description;

    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private Long ownerUserId;
}
