package com.viswa.crm.service;

import com.viswa.crm.dto.contact.ContactResponse;
import com.viswa.crm.dto.contact.CreateContactRequest;
import com.viswa.crm.dto.contact.UpdateContactRequest;
import com.viswa.crm.dto.contact.view.ContactDetailResponse;

import java.util.List;

public interface ContactService {

    ContactResponse createContact(CreateContactRequest request, Long currentUserId, String roleName);

    ContactResponse updateContact(Long contactId, UpdateContactRequest request, Long currentUserId, String roleName);

    void deleteContact(Long contactId, Long currentUserId, String roleName);

    ContactResponse getContactById(Long contactId, Long currentUserId, String roleName);

    List<ContactResponse> getContactsByCompany(Long companyId, Long currentUserId, String roleName);

    List<ContactResponse> searchContacts(String keyword, Long currentUserId, String roleName);

    ContactDetailResponse getContactDetail(
            Long contactId,
            Long currentUserId,
            String roleName
    );

}
