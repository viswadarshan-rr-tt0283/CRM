package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RecentItemDto {

    private Long id;
    private String title;
    private String type;      // DEAL, CONTACT, ACTIVITY
    private LocalDateTime createdAt;
}
