package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.activity.ActivityResponse;
import com.viswa.crm.dto.activity.CreateActivityRequest;
import com.viswa.crm.dto.activity.UpdateActivityRequest;
import com.viswa.crm.dto.activity.view.ActivityViewResponse;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.model.ActivityType;
import com.viswa.crm.service.ActivityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
public class ActivityAction extends BaseAction {

    @Autowired
    private transient ActivityService activityService;

    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    private Long id;
    private Long dealId;
    private ActivityType type;
    private String description;
    private LocalDate dueDate;
    private String statusCode;

    @Action(
            value = "activity",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
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

        List<ActivityResponse> activities =
                activityService.getActivitiesForUser(
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName(),
                        100
                );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Activities fetched successfully");
        apiResponse.setData(activities);

        return SUCCESS;
    }

    @Action(
            value = "activity-save",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String save() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canCreateActivity(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to create activity");
            return ERROR;
        }

        CreateActivityRequest request = new CreateActivityRequest();
        request.setDealId(dealId);
        request.setType(type);
        request.setDescription(description);
        request.setDueDate(dueDate);

        ActivityResponse response =
                activityService.createActivity(
                        request,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Activity created successfully");
        apiResponse.setData(response);

        return SUCCESS;
    }

    @Action(
            value = "activity-update",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String update() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canEditActivity(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to edit activity");
            return ERROR;
        }

        UpdateActivityRequest request = new UpdateActivityRequest();
        request.setType(type);
        request.setDescription(description);
        request.setDueDate(dueDate);
        request.setStatusCode(statusCode);

        ActivityResponse response =
                activityService.updateActivity(
                        id,
                        request,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Activity updated successfully");
        apiResponse.setData(response);

        return SUCCESS;
    }

    @Action(
            value = "activity-status",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String updateStatus() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canEditActivity(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to update activity status");
            return ERROR;
        }

        UpdateActivityRequest request = new UpdateActivityRequest();
        request.setStatusCode(statusCode);

        ActivityResponse response =
                activityService.updateActivity(
                        id,
                        request,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Activity status updated successfully");
        apiResponse.setData(response);

        return SUCCESS;
    }

    @Action(
            value = "activity-delete",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String delete() {

        if (!isAuthenticated()) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authenticated");
            return LOGIN;
        }

        if (!role().canDeleteActivity(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to delete activity");
            return ERROR;
        }

        activityService.deleteActivity(
                id,
                getCurrentUser().getUserId(),
                getCurrentUser().getRoleName()
        );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Activity deleted successfully");
        apiResponse.setData(null);

        return SUCCESS;
    }

    @Action(
            value = "activity-status-options",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse"})
            }
    )
    public String statusOptions() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        Map<String, Object> data =
                activityService.getStatusOptions(type, statusCode);

        apiResponse.success("Status options fetched", data);
        return SUCCESS;
    }

    @Action(
            value = "activity-view",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String view() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        ActivityViewResponse response =
                activityService.getActivityView(
                        id,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        apiResponse.success("Activity view fetched", response);
        return SUCCESS;
    }


}
