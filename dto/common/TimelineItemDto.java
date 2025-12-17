package com.viswa.crm.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TimelineItemDto {

    private String entityType;   // COMPANY, CONTACT, DEAL, ACTIVITY
    private Long entityId;

    private String title;
    private String subtitle;

    private LocalDateTime createdAt;
}
