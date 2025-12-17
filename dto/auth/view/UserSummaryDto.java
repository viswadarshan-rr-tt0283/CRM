package com.viswa.crm.dto.auth.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSummaryDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
}
