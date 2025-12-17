package com.viswa.crm.service.impl;

import com.viswa.crm.dto.company.view.ContactSummaryDto;
import com.viswa.crm.dto.contact.view.CompanySummaryDto;
import com.viswa.crm.dto.deal.ChangeDealStatusRequest;
import com.viswa.crm.dto.deal.CreateDealRequest;
import com.viswa.crm.dto.deal.DealResponse;
import com.viswa.crm.dto.deal.UpdateDealRequest;
import com.viswa.crm.dto.deal.view.ActivitySummaryDto;
import com.viswa.crm.dto.deal.view.DealDetailResponse;
import com.viswa.crm.model.*;
import com.viswa.crm.repository.*;
import com.viswa.crm.service.DealService;
import com.viswa.crm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final ActivityRepository activityRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForUser(
            Long currentUserId,
            String roleName,
            String keyword
    ) {

        List<Deal> deals;

        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_ALL")) {

            deals = dealRepository.search(keyword == null ? "" : keyword);

        } else if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_TEAM")) {

            List<Long> teamUserIds =
                    userRepository.findUserIdsByManagerId(currentUserId);
            teamUserIds.add(currentUserId);

            deals = dealRepository.findByUserId(teamUserIds, keyword);

        } else if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_SELF")) {

        if (keyword == null || keyword.isBlank()) {
            deals = dealRepository.findByUserId(currentUserId);
        } else {
            deals = dealRepository.searchByUserId(currentUserId, keyword);
        }
    } else {
            throw new RuntimeException("Access denied");
        }

        return deals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DealResponse getDealById(
            Long dealId,
            Long currentUserId,
            String roleName
    ) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        enforceDealAccess(deal, currentUserId, roleName);
        return mapToResponse(deal);
    }

    @Override
    @Transactional
    public DealResponse createDeal(
            CreateDealRequest request,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "DEAL_CREATE")) {
            throw new RuntimeException("No permission to create deal");
        }

        User owner = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Contact contact = null;
        if (request.getContactId() != null) {
            contact = contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found"));

            if (!contact.getCompany().getId().equals(company.getId())) {
                throw new RuntimeException("Contact does not belong to company");
            }
        }

        Deal deal = new Deal();
        deal.setTitle(request.getTitle());
        deal.setAmount(request.getAmount());
        deal.setStatus(DealStatus.NEW);
        deal.setAssignedUser(owner);
        deal.setCompany(company);
        deal.setContact(contact);
        deal.setCreatedAt(LocalDateTime.now());

        Long id = dealRepository.save(deal);
        deal.setId(id);

        return mapToResponse(deal);
    }

    @Override
    @Transactional
    public DealResponse updateDeal(
            Long dealId,
            UpdateDealRequest request,
            Long currentUserId,
            String roleName
    ) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        enforceDealAccess(deal, currentUserId, roleName);

        if (!permissionService.hasPermission(currentUserId, "DEAL_UPDATE")) {
            throw new RuntimeException("No permission to update deal");
        }

        if (request.getTitle() != null) {
            deal.setTitle(request.getTitle());
        }

        if (request.getAmount() != null) {
            deal.setAmount(request.getAmount());
        }

        if (request.getAssignedUserId() != null) {

            if (!permissionService.hasPermission(currentUserId, "DEAL_REASSIGN")) {
                throw new RuntimeException("Access denied");
            }

            User newOwner = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            deal.setAssignedUser(newOwner);
        }

        if (request.getContactId() != null) {
            Contact contact = contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found"));

            if (!contact.getCompany().getId().equals(deal.getCompany().getId())) {
                throw new RuntimeException("Contact does not belong to deal company");
            }

            deal.setContact(contact);
        }

        dealRepository.update(deal);
        return mapToResponse(deal);
    }

    @Override
    @Transactional
    public void deleteDeal(
            Long dealId,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "DEAL_DELETE")) {
            throw new RuntimeException("No permission to delete deal");
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (deal.getStatus() != DealStatus.CLOSED) {
            throw new RuntimeException("Only closed deals can be deleted");
        }

        if (activityRepository.existsByDealId(dealId)) {
            throw new RuntimeException("Deal has activities");
        }

        dealRepository.deleteById(dealId);
    }

    @Override
    @Transactional
    public DealResponse changeDealStatus(
            Long dealId,
            ChangeDealStatusRequest request,
            Long currentUserId,
            String roleName
    ) {

        if (!permissionService.hasPermission(currentUserId, "DEAL_CHANGE_STATUS")) {
            throw new RuntimeException("No permission to change deal status");
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        enforceDealAccess(deal, currentUserId, roleName);

        validateStatusTransition(deal.getStatus(), request.getStatus());

        deal.setStatus(request.getStatus());
        dealRepository.update(deal);

        return mapToResponse(deal);
    }

    private void enforceDealAccess(
            Deal deal,
            Long currentUserId,
            String roleName
    ) {

        // 1. Full access (ADMIN or similar)
        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_ALL")) {
            return;
        }

        User assignedUser = deal.getAssignedUser();
        if (assignedUser == null || assignedUser.getId() == null) {
            throw new RuntimeException("Access denied");
        }

        // 2. Own deal access
        if (assignedUser.getId().equals(currentUserId)) {
            if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_SELF")) {
                return;
            }
        }

        // 3. Team deal access (manager only)
        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_TEAM")) {

            User ownerManager = assignedUser.getManager();

            if (ownerManager != null
                    && ownerManager.getId().equals(currentUserId)) {
                return;
            }
        }

        // 4. Default deny
        throw new RuntimeException("Access denied");
    }


    private void validateStatusTransition(DealStatus current, DealStatus next) {
        if (current == DealStatus.CLOSED) {
            throw new RuntimeException("Closed deals cannot be modified");
        }

        switch (current) {
            case NEW:
                if (next != DealStatus.QUALIFIED) {
                    throw new RuntimeException("Invalid transition");
                }
                break;
            case QUALIFIED:
                if (next != DealStatus.IN_PROGRESS) {
                    throw new RuntimeException("Invalid transition");
                }
                break;
            case IN_PROGRESS:
                if (next != DealStatus.DELIVERED) {
                    throw new RuntimeException("Invalid transition");
                }
                break;
            case DELIVERED:
                if (next != DealStatus.CLOSED) {
                    throw new RuntimeException("Invalid transition");
                }
                break;
            default:
                break;
        }
    }

    private DealResponse mapToResponse(Deal deal) {
        return DealResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .amount(deal.getAmount())
                .status(deal.getStatus())
                .createdAt(deal.getCreatedAt())
                .assignedUserId(deal.getAssignedUser().getId())
                .assignedUserName(deal.getAssignedUser().getFullName())
                .companyId(deal.getCompany().getId())
                .companyName(deal.getCompany().getName())
                .contactId(deal.getContact() != null ? deal.getContact().getId() : null)
                .contactName(deal.getContact() != null ? deal.getContact().getName() : null)
                .build();
    }

    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForAdmin(String keyword) {

        List<Deal> deals =
                (keyword == null || keyword.isBlank())
                        ? dealRepository.search("")
                        : dealRepository.search(keyword);

        return deals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForSales(Long userId, String keyword) {

        List<Deal> deals;

        if (keyword == null || keyword.isBlank()) {
            deals = dealRepository.findByUserId(userId);
        } else {
            deals = dealRepository.searchByUserId(userId, keyword);
        }

        return deals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForManager(Long managerId, String keyword) {
        List<Long> teamUserIds =
                userRepository.findUserIdsByManagerId(managerId);

        teamUserIds.add(managerId);
        List<Deal> deals;

        if (keyword == null || keyword.isBlank()) {
            deals = dealRepository.findByUserIds(teamUserIds);
        } else {
            deals = dealRepository.searchByUserIds(keyword, teamUserIds);
        }
        return deals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DealDetailResponse getDealDetail(
            Long dealId,
            Long currentUserId,
            String roleName
    ) {

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        enforceDealAccess(deal, currentUserId, roleName);

        List<ActivitySummaryDto> activities =
                activityRepository.findByDealId(dealId)
                        .stream()
                        .map(this::mapActivity)
                        .collect(Collectors.toList());

        return DealDetailResponse.builder()
                .deal(mapToResponse(deal))
                .company(mapCompany(deal.getCompany()))
                .contact(
                        deal.getContact() != null
                                ? mapContact(deal.getContact())
                                : null
                )
                .activities(activities)
                .allowedNextStatuses(
                        computeNextStatuses(deal.getStatus())
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

    private ContactSummaryDto mapContact(Contact c) {
        return ContactSummaryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .jobTitle(c.getJobTitle())
                .build();
    }

    private ActivitySummaryDto mapActivity(Activity a) {
        return ActivitySummaryDto.builder()
                .id(a.getId())
                .type(a.getActivityType().name())
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .statusCode(a.getStatusCode())
                .ownerId(a.getOwnerUserId())
                .build();
    }

    private List<DealStatus> computeNextStatuses(DealStatus current) {

        if (current == DealStatus.CLOSED) {
            return List.of();
        }

        switch (current) {
            case NEW:
                return List.of(DealStatus.QUALIFIED);
            case QUALIFIED:
                return List.of(DealStatus.IN_PROGRESS);
            case IN_PROGRESS:
                return List.of(DealStatus.DELIVERED);
            case DELIVERED:
                return List.of(DealStatus.CLOSED);
            default:
                return List.of();
        }
    }


}
