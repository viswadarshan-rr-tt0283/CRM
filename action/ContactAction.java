package com.viswa.crm.action;

import com.viswa.crm.action.base.BaseAction;
import com.viswa.crm.dto.common.ApiResponse;
import com.viswa.crm.dto.contact.ContactResponse;
import com.viswa.crm.dto.contact.CreateContactRequest;
import com.viswa.crm.dto.contact.UpdateContactRequest;
import com.viswa.crm.dto.contact.view.ContactDetailResponse;
import com.viswa.crm.service.ContactService;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@AllowedMethods({"execute", "add", "edit", "save", "delete", "view"})
@Component
@Getter
@Setter
@ParentPackage("json-default")
@Namespace("/")
public class ContactAction extends BaseAction {

    @Autowired
    private transient ContactService contactService;

    private ApiResponse<Object> apiResponse = new ApiResponse<>();

    // Request / filter fields
    private Long id;
    private Long companyId;
    private String name;
    private String email;
    private String phone;
    private String jobTitle;
    private String keyword;

    /* =========================================================
       LIST / SEARCH
       ========================================================= */
    @Action(
            value = "contact",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    @Override
    public String execute() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        Long currentUserId = getCurrentUser().getUserId();
        String roleName = getCurrentUser().getRoleName();

        List<ContactResponse> contacts;

        if (companyId != null) {
            contacts = contactService.getContactsByCompany(
                    companyId,
                    currentUserId,
                    roleName
            );
            apiResponse.success("Contacts for company fetched successfully", contacts);
            return SUCCESS;
        }

        contacts = contactService.searchContacts(
                keyword,
                currentUserId,
                roleName
        );

        apiResponse.success("Contacts fetched successfully", contacts);
        return SUCCESS;
    }

    /* =========================================================
       ADD
       ========================================================= */
    @Action(
            value = "contact-add",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String add() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canCreateContact(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized to create contact");
            return ERROR;
        }

        apiResponse.success("Ready to create contact", null);
        return SUCCESS;
    }

    /* =========================================================
       EDIT
       ========================================================= */
    @Action(
            value = "contact-edit",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String edit() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        Long currentUserId = getCurrentUser().getUserId();
        String roleName = getCurrentUser().getRoleName();

        ContactResponse contact =
                contactService.getContactById(id, currentUserId, roleName);

        apiResponse.success("Contact fetched successfully", contact);
        return SUCCESS;
    }

    /* =========================================================
       CREATE / UPDATE
       ========================================================= */
    @Action(
            value = "contact-save",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = INPUT, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String save() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        Long currentUserId = getCurrentUser().getUserId();
        String roleName = getCurrentUser().getRoleName();

        try {
            if (id == null) {

                if (!role().canCreateContact(getCurrentUser().getUserId())) {
                    apiResponse.fail("Not authorized to create contact");
                    return ERROR;
                }

                CreateContactRequest request = new CreateContactRequest();
                request.setCompanyId(companyId);
                request.setName(name);
                request.setEmail(email);
                request.setPhone(phone);
                request.setJobTitle(jobTitle);

                contactService.createContact(
                        request,
                        currentUserId,
                        roleName
                );

                apiResponse.success("Contact created successfully", null);

            } else {

                if (!role().canEditContact(getCurrentUser().getUserId())) {
                    apiResponse.fail("Not authorized to update contact");
                    return ERROR;
                }

                UpdateContactRequest request = new UpdateContactRequest();
                request.setName(name);
                request.setEmail(email);
                request.setPhone(phone);
                request.setJobTitle(jobTitle);

                contactService.updateContact(
                        id,
                        request,
                        currentUserId,
                        roleName
                );

                apiResponse.success("Contact updated successfully", null);
            }

            return SUCCESS;

        } catch (Exception ex) {
            apiResponse.fail(ex.getMessage());
            return INPUT;
        }
    }

    /* =========================================================
       DELETE
       ========================================================= */
    @Action(
            value = "contact-delete",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String delete() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        if (!role().canDeleteContact(getCurrentUser().getUserId())) {
            apiResponse.fail("Not authorized to delete contact");
            return ERROR;
        }

        Long currentUserId = getCurrentUser().getUserId();
        String roleName = getCurrentUser().getRoleName();

        contactService.deleteContact(
                id,
                currentUserId,
                roleName
        );

        apiResponse.success("Contact deleted successfully", null);
        return SUCCESS;
    }

    @Action(
            value = "contact-view",
            results = {
                    @Result(name = SUCCESS, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = LOGIN, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"}),
                    @Result(name = ERROR, type = "json",
                            params = {"root", "apiResponse", "excludeNullProperties", "true"})
            }
    )
    public String view() {

        if (!isAuthenticated()) {
            apiResponse.fail("Not authenticated");
            return LOGIN;
        }

        Long userId = getCurrentUser().getUserId();
        String role = getCurrentUser().getRoleName();

        ContactDetailResponse detail =
                contactService.getContactDetail(id, userId, role);

        apiResponse.success("Contact detail fetched successfully", detail);
        return SUCCESS;
    }

}
