package com.viswa.crm.dto.company.view;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContactSummaryDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String jobTitle;
    private LocalDateTime createdAt;
}
