package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.dto.role.CreateRoleRequest;
import com.viswa.crm.dto.role.PermissionResponse;
import com.viswa.crm.dto.role.RoleResponse;
import com.viswa.crm.service.RoleService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
public class RoleAction extends BaseAction {

    @Autowired
    private transient RoleService roleService;

    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    /* ===== Inputs ===== */
    private Long roleId;
    private String roleName;
    private List<Long> permissionIds;
    private Long userId;

    /* =========================================================
       LIST ROLES
       ========================================================= */
    @Action(
            value = "role",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String list() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        List<RoleResponse> roles = roleService.getAllRoles();

        apiResponse.success("Roles fetched", roles);
        return SUCCESS;
    }

    /* =========================================================
       CREATE ROLE
       ========================================================= */
    @Action(
            value = "role-create",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String create() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized");
            return ERROR;
        }

        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName(roleName);
        request.setPermissionIds(permissionIds);

        RoleResponse response = roleService.createRole(request);

        apiResponse.success("Role created", response);
        return SUCCESS;
    }

    /* =========================================================
       UPDATE ROLE PERMISSIONS
       ========================================================= */
    @Action(
            value = "role-update",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String updatePermissions() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized");
            return ERROR;
        }

        RoleResponse response =
                roleService.updateRolePermissions(roleId, permissionIds);

        apiResponse.success("Role updated", response);
        return SUCCESS;
    }

    @Action(
            value = "role-delete",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String delete() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized");
            return ERROR;
        }

        roleService.deleteRole(roleId);

        apiResponse.success("Role deleted", null);
        return SUCCESS;
    }

    @Action(
            value = "role-permissions",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String permissions() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        List<PermissionResponse> permissions =
                roleService.getAllPermissions();

        apiResponse.success("Permissions fetched", permissions);
        return SUCCESS;
    }

    @Action(
            value = "user-assign-role",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String assignRole() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized");
            return ERROR;
        }

        roleService.assignRoleToUser(userId, roleId);

        apiResponse.success("Role assigned to user", null);
        return SUCCESS;
    }

    @Action(
            value = "role-view",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String view() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized");
            return ERROR;
        }

        RoleResponse response = roleService.getRoleById(roleId);

        apiResponse.success("Role fetched", response);
        return SUCCESS;
    }

}
