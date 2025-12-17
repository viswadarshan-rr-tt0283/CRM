package com.viswa.crm.dto.dashboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserContextDto {

    private Long userId;
    private String username;
    private String fullName;
    private String role;
}
