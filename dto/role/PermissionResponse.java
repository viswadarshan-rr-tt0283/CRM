package com.viswa.crm.dto.role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionResponse {

    private Long id;
    private String permissionCode;
    private String description;
}
