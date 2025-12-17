package com.viswa.crm.dto.activity.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanySummaryDto {

    private Long id;

    private String name;

    private String email;

    private String phone;
}
