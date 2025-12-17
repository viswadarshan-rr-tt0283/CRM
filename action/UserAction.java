package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.auth.CreateUserRequest;
import com.viswa.crm.dto.auth.UserResponse;
import com.viswa.crm.dto.auth.view.UserViewResponse;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.service.AuthService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.INPUT;
import static com.opensymphony.xwork2.Action.LOGIN;
import static com.opensymphony.xwork2.Action.SUCCESS;

@AllowedMethods({"execute", "add", "edit", "save", "delete", "view"})
@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
public class UserAction extends BaseAction {

    @Autowired
    private transient AuthService authService;   // do not serialize

    // JSON root
    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    // form fields
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private Long roleId;

    // ===== LIST USERS =====
    @Action(
            value = "user",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    @Override
    public String execute() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            apiResponse.setData(null);
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to manage users");
            apiResponse.setData(null);
            return ERROR;
        }

        List<UserResponse> users = authService.getAllUsers();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Users fetched successfully");
        apiResponse.setData(users);

        return SUCCESS;
    }

    // ===== PREPARE ADD (role options) =====
    @Action(
            value = "user-add",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String add() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to manage users");
            return ERROR;
        }

        Map<Long, String> roleOptions = buildRoleOptions();

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Ready to create user");
        apiResponse.setData(roleOptions);

        clearForm();
        return SUCCESS;
    }

    // ===== CREATE USER =====
    @Action(
            value = "user-save",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = INPUT,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String save() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to manage users");
            return ERROR;
        }

        try {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setFullName(fullName);
            request.setRoleId(roleId);

            authService.createUser(request);

            apiResponse.setSuccess(true);
            apiResponse.setMessage("User created successfully");
            apiResponse.setData(null);
            return SUCCESS;

        } catch (Exception ex) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage(ex.getMessage());
            apiResponse.setData(buildRoleOptions());
            return INPUT;
        }
    }

    // ===== DELETE USER =====
    @Action(
            value = "user-delete",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR,   type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String delete() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to manage users");
            return ERROR;
        }

        try {
            authService.deleteUser(id);
            apiResponse.setSuccess(true);
            apiResponse.setMessage("User deleted successfully");
            apiResponse.setData(null);
            return SUCCESS;

        } catch (Exception ex) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage(ex.getMessage());
            apiResponse.setData(null);
            return ERROR;
        }
    }

    private void clearForm() {
        this.id = null;
        this.username = null;
        this.email = null;
        this.password = null;
        this.fullName = null;
        this.roleId = null;
    }

    private Map<Long, String> buildRoleOptions() {
        Map<Long, String> roles = new LinkedHashMap<>();
        roles.put(1L, "ADMIN");
        roles.put(2L, "MANAGER");
        roles.put(3L, "SALES");
        return roles;
    }

    @Action(
            value = "user-view-detail",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String viewDetail() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canManageUsers(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized to view user details");
            return ERROR;
        }

        UserViewResponse data = authService.getUserView(id);

        apiResponse.success("User details fetched", data);
        return SUCCESS;
    }
}
