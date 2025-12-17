package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.dto.dashboard.DashboardResponse;
import com.viswa.crm.service.DashboardService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.opensymphony.xwork2.Action.LOGIN;
import static com.opensymphony.xwork2.Action.SUCCESS;

@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
@AllowedMethods({"execute"})
public class DashboardAction extends BaseAction {

    @Autowired
    private transient DashboardService dashboardService;

    // JSON root
    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    // Optional UI params
    private Integer recentLimit;

    @Action(
            value = "dashboard",
            results = {
                    @Result(
                            name = SUCCESS,
                            type = "json",
                            params = {
                                    "root", "apiResponse",
                                    "excludeNullProperties", "true"
                            }
                    ),
                    @Result(
                            name = LOGIN,
                            type = "json",
                            params = {
                                    "root", "apiResponse",
                                    "excludeNullProperties", "true"
                            }
                    )
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

        int limit = (recentLimit == null || recentLimit <= 0)
                ? 10
                : recentLimit;

        DashboardResponse dashboard =
                dashboardService.loadDashboard(
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName(),
                        limit
                );

        apiResponse.setSuccess(true);
        apiResponse.setMessage("Dashboard loaded successfully");
        apiResponse.setData(dashboard);

        return SUCCESS;
    }
}
