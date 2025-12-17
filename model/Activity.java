package com.viswa.crm.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Activity {

    private Long id;

    // Business context
    private Deal deal;

    // Ownership (CRITICAL)
    private Long ownerUserId;

    // Activity nature
    private ActivityType activityType;
    private String statusCode;

    // Core data
    private String description;
    private LocalDate dueDate;

    // Audit
    private LocalDateTime createdAt;
}
