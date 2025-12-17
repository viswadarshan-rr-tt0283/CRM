package com.viswa.crm.dto.auth.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserViewResponse {

    // User info
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String roleName;

    // Manager info
    private Long managerId;
    private String managerName;

    // Relations
    private List<DealSummaryDto> deals;
    private List<UserSummaryDto> teamMembers;
}
