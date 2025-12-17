package com.viswa.crm.dto.contact.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanySummaryDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
}
