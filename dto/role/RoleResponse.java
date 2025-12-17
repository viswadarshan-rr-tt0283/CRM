package com.viswa.crm.dto.role;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleResponse {

    private Long id;
    private String roleName;
    private List<String> permissions;
}
