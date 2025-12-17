package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.auth.UserResponse;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.dto.company.CompanyResponse;
import com.viswa.crm.dto.contact.ContactResponse;
import com.viswa.crm.dto.deal.ChangeDealStatusRequest;
import com.viswa.crm.dto.deal.CreateDealRequest;
import com.viswa.crm.dto.deal.DealResponse;
import com.viswa.crm.dto.deal.UpdateDealRequest;
import com.viswa.crm.dto.deal.view.DealDetailResponse;
import com.viswa.crm.model.DealStatus;
import com.viswa.crm.service.AuthService;
import com.viswa.crm.service.CompanyService;
import com.viswa.crm.service.ContactService;
import com.viswa.crm.service.DealService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.opensymphony.xwork2.Action.*;

@AllowedMethods({"execute", "add", "edit", "save", "delete", "changeStatus", "view"})
@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
public class DealAction extends BaseAction {

    @Autowired
    private transient DealService dealService;
    @Autowired
    private transient CompanyService companyService;
    @Autowired
    private transient ContactService contactService;
    @Autowired
    private transient AuthService authService;

    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    // Request params
    private Long id;
    private String title;
    private BigDecimal amount;
    private Long assignedUserId;
    private Long companyId;
    private Long contactId;
    private DealStatus status;
    private String keyword;

    // Dropdown data
    private List<UserResponse> users;
    private List<CompanyResponse> companies;
    private List<ContactResponse> contacts;
    private DealStatus[] statuses = DealStatus.values();

    @Action(
            value = "deal",
            results = {
                    @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"}),
                    @Result(name = LOGIN, type = "json", params = {"root", "apiResponse"})
            }
    )
    @Override
    public String execute() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        List<DealResponse> deals =
                dealService.getDealsForUser(
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName(),
                        keyword
                );

        apiResponse.success("Deals fetched successfully", deals);
        return SUCCESS;
    }

    @Action(
            value = "deal-add",
            results = @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"})
    )
    public String add() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canCreateDeal(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to Create Deal");
            return ERROR;
        }

        loadDropdowns(null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("statuses", statuses);
        payload.put("companies", companies);
        payload.put("users", users);
        payload.put("contacts", List.of()); // frontend loads based on company selection


        apiResponse.success("Ready to create deal", payload);
        return SUCCESS;
    }

    @Action(
            value = "deal-edit",
            results = @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"})
    )
    public String edit() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canEditDeal(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to edit Deal");
            return ERROR;
        }

        DealResponse deal =
                dealService.getDealById(
                        id,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        loadDropdowns(deal.getCompanyId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("deal", deal);
        payload.put("companies", companies);
        payload.put("users", users);
        payload.put("contacts", contacts);
        payload.put("statuses", statuses);

        apiResponse.success("Deal fetched successfully", payload);

        apiResponse.success("Deal fetched successfully", deal);
        return SUCCESS;
    }

    @Action(
            value = "deal-save",
            results = {
                    @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"}),
                    @Result(name = ERROR, type = "json", params = {"root", "apiResponse"})
            }
    )
    public String save() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canCreateDeal(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to Create Deal");
            return ERROR;
        }

        if (companyId == null) {
            apiResponse.fail("Company is required");
            return ERROR;
        }

        if (assignedUserId == null) {
            apiResponse.fail("Assigned user is required");
            return ERROR;
        }

        try {
            if (id == null) {

                CreateDealRequest request = new CreateDealRequest();
                request.setTitle(title);
                request.setAmount(amount);
                request.setCompanyId(companyId);
                request.setContactId(contactId);
                request.setAssignedUserId(assignedUserId);

                dealService.createDeal(
                        request,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

                apiResponse.success("Deal created successfully", null);

            } else {

                UpdateDealRequest request = new UpdateDealRequest();
                request.setTitle(title);
                request.setAmount(amount);
                request.setAssignedUserId(assignedUserId);
                request.setContactId(contactId);

                dealService.updateDeal(
                        id,
                        request,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

                apiResponse.success("Deal updated successfully", null);
            }

            return SUCCESS;

        } catch (RuntimeException ex) {
            apiResponse.fail(ex.getMessage());
            return ERROR;
        }
    }

    @Action(
            value = "deal-delete",
            results = @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"})
    )
    public String delete() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canDeleteDeal(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to delete deal");
            return ERROR;
        }

        dealService.deleteDeal(
                id,
                getCurrentUser().getUserId(),
                getCurrentUser().getRoleName()
        );

        apiResponse.success("Deal deleted successfully", null);
        return SUCCESS;
    }

    @Action(
            value = "deal-changeStatus",
            results = @Result(name = SUCCESS, type = "json", params = {"root", "apiResponse"})
    )
    public String changeStatus() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canChangeDealStatus(getCurrentUser().getUserId())) {
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Not authorized to change deal status");
            return ERROR;
        }

        ChangeDealStatusRequest request = new ChangeDealStatusRequest();
        request.setStatus(status);

        dealService.changeDealStatus(
                id,
                request,
                getCurrentUser().getUserId(),
                getCurrentUser().getRoleName()
        );

        apiResponse.success("Deal status changed successfully", null);
        return SUCCESS;
    }

    private void loadDropdowns() {
        companies = companyService.getAllCompanies();
        users = authService.getAllUsers();
        contacts =
                companyId != null
                        ? contactService.getContactsByCompany(companyId, getCurrentUser().getUserId(), getCurrentUser().getRoleName())
                        : List.of();
    }

    private void loadDropdowns(Long companyIdForContacts) {
        companies = companyService.getAllCompanies();
        users = authService.getAllUsers();

        contacts =
                companyIdForContacts != null
                        ? contactService.getContactsByCompany(
                        companyIdForContacts,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                )
                        : List.of();
    }


    @Action(
            value = "deal-view",
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
                                    "root", "apiResponse"
                            }
                    )
            }
    )
    public String view() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        DealDetailResponse response =
                dealService.getDealDetail(
                        id,
                        getCurrentUser().getUserId(),
                        getCurrentUser().getRoleName()
                );

        apiResponse.success("Deal detail fetched successfully", response);
        return SUCCESS;
    }
}
