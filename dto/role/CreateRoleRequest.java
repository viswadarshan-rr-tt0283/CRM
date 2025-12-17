package com.viswa.crm.dto.role;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRoleRequest {

    private String roleName;
    private List<Long> permissionIds;
}
