package com.viswa.crm.service.impl;

import com.viswa.crm.dto.contact.ContactResponse;
import com.viswa.crm.dto.contact.CreateContactRequest;
import com.viswa.crm.dto.contact.UpdateContactRequest;
import com.viswa.crm.dto.contact.view.ActivitySummaryDto;
import com.viswa.crm.dto.contact.view.CompanySummaryDto;
import com.viswa.crm.dto.contact.view.ContactDetailResponse;
import com.viswa.crm.dto.contact.view.DealSummaryDto;
import com.viswa.crm.model.Activity;
import com.viswa.crm.model.Company;
import com.viswa.crm.model.Contact;
import com.viswa.crm.model.Deal;
import com.viswa.crm.repository.*;
import com.viswa.crm.service.ContactService;
import com.viswa.crm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public ContactResponse createContact(
            CreateContactRequest request,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "CONTACT_CREATE")) {
            throw new RuntimeException("No permission to create contact");
        }

        if (!companyRepository.existsById(request.getCompanyId())) {
            throw new RuntimeException("Company not found");
        }

        Company company = new Company();
        company.setId(request.getCompanyId());

        Contact contact = new Contact();
        contact.setCompany(company);
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setJobTitle(request.getJobTitle());
        contact.setCreatedAt(LocalDateTime.now());

        Long id = contactRepository.save(contact);
        contact.setId(id);

        String companyName =
                companyRepository.findById(company.getId())
                        .map(Company::getName)
                        .orElse(null);

        return mapToResponse(contact, companyName);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(
            Long contactId,
            UpdateContactRequest request,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "CONTACT_UPDATE")) {
            throw new RuntimeException("No permission to update contact");
        }

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        enforceContactAccess(contactId, currentUserId);

        if (request.getName() != null) {
            contact.setName(request.getName());
        }
        if (request.getEmail() != null) {
            contact.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }
        if (request.getJobTitle() != null) {
            contact.setJobTitle(request.getJobTitle());
        }

        contactRepository.update(contact);

        String companyName =
                companyRepository.findById(contact.getCompany().getId())
                        .map(Company::getName)
                        .orElse(null);

        return mapToResponse(contact, companyName);
    }

    @Override
    @Transactional
    public void deleteContact(
            Long contactId,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "CONTACT_DELETE")) {
            throw new RuntimeException("No permission to delete contact");
        }

        if (dealRepository.existsActiveDealByContactId(contactId)) {
            throw new RuntimeException("Cannot delete contact with active deals");
        }

        contactRepository.deleteById(contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContactById(
            Long contactId,
            Long currentUserId,
            String roleName
    ) {

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        enforceContactAccess(contactId, currentUserId);

        String companyName =
                companyRepository.findById(contact.getCompany().getId())
                        .map(Company::getName)
                        .orElse(null);

        return mapToResponse(contact, companyName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getContactsByCompany(
            Long companyId,
            Long currentUserId,
            String roleName
    ) {

        if (!companyRepository.existsById(companyId)) {
            throw new RuntimeException("Company not found");
        }

        List<Long> allowedContactIds =
                resolveAccessibleContactIds(currentUserId);

        return contactRepository.findByCompanyId(companyId)
                .stream()
                .filter(c -> allowedContactIds.contains(c.getId()))
                .map(c -> mapToResponse(c, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> searchContacts(
            String keyword,
            Long currentUserId,
            String roleName
    ) {

        List<Long> allowedContactIds = resolveAccessibleContactIds(currentUserId);

        return contactRepository.searchByKeyword(
                        keyword == null ? "" : keyword)
                .stream()
                .filter(c -> allowedContactIds.contains(c.getId()))
                .map(c -> mapToResponse(c, null))
                .collect(Collectors.toList());
    }

    private void enforceContactAccess(
            Long contactId,
            Long currentUserId
    ) {

        // 1. Full access
        if (permissionService.hasPermission(currentUserId, "CONTACT_VIEW_ALL")) {
            return;
        }

        // 2. Resolve visible contacts
        List<Long> allowedContactIds =
                resolveAccessibleContactIds(currentUserId);

        if (!allowedContactIds.contains(contactId)) {
            throw new RuntimeException("Access denied");
        }
    }
    private List<Long> resolveAccessibleContactIds(Long currentUserId) {

        // 1. Admin-like access
        if (permissionService.hasPermission(currentUserId, "CONTACT_VIEW_ALL")) {
            return contactRepository.searchByKeyword("")
                    .stream()
                    .map(Contact::getId)
                    .collect(Collectors.toList());
        }

        List<Deal> visibleDeals;

        // 2. Manager team visibility
        if (permissionService.hasPermission(currentUserId, "CONTACT_VIEW_TEAM")) {

            List<Long> teamUserIds =
                    userRepository.findUserIdsByManagerId(currentUserId);
            teamUserIds.add(currentUserId);

            visibleDeals = dealRepository.findByUserIds(teamUserIds);

        }
        // 3. Self visibility
        else if (permissionService.hasPermission(currentUserId, "CONTACT_VIEW_SELF")) {

            visibleDeals = dealRepository.findByUserId(currentUserId);

        } else {
            return List.of();
        }

        // 4. Extract distinct contact IDs
        return visibleDeals.stream()
                .map(Deal::getContact)
                .filter(Objects::nonNull)
                .map(Contact::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    private ContactResponse mapToResponse(Contact contact, String companyName) {

        return ContactResponse.builder()
                .id(contact.getId())
                .companyId(
                        contact.getCompany() != null
                                ? contact.getCompany().getId()
                                : null
                )
                .companyName(
                        contact.getCompany() != null
                                ? contact.getCompany().getName()
                                : null
                )
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .jobTitle(contact.getJobTitle())
                .createdAt(contact.getCreatedAt())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ContactDetailResponse getContactDetail(
            Long contactId,
            Long currentUserId,
            String roleName
    ) {

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // RBAC
        enforceContactAccess(contactId, currentUserId);

        // Company
        Company company = companyRepository.findById(contact.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Deals
        List<Deal> deals = dealRepository.findByContactId(contactId);

        // Activities
        List<Activity> activities =
                activityRepository.findByContactId(contactId);

        return ContactDetailResponse.builder()
                .contact(mapToResponse(contact, company.getName()))
                .company(mapCompany(company))
                .deals(
                        deals.stream()
                                .map(this::mapDeal)
                                .collect(Collectors.toList())
                )
                .activities(
                        activities.stream()
                                .map(this::mapActivity)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private CompanySummaryDto mapCompany(Company c) {
        return CompanySummaryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .build();
    }

    private DealSummaryDto mapDeal(Deal d) {
        return DealSummaryDto.builder()
                .id(d.getId())
                .title(d.getTitle())
                .amount(d.getAmount())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private ActivitySummaryDto mapActivity(Activity a) {
        return ActivitySummaryDto.builder()
                .id(a.getId())
                .type(a.getActivityType())
                .statusCode(a.getStatusCode())
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .createdAt(a.getCreatedAt())
                .build();
    }

}
