package com.viswa.crm.dto.deal.view;

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
    private String type;
    private String description;
    private LocalDate dueDate;
    private String statusCode;

    private Long ownerId;
//    private String ownerName;
}