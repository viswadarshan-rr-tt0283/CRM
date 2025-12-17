package com.viswa.crm.dto.activity.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActivityStatusOptionsDto {

    private String current;

    private boolean terminal;

    private List<NextStatusDto> allowedNext;
}
