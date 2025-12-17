package com.viswa.crm.dto.activity.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextStatusDto {

    private String code;

    private String label;
}
