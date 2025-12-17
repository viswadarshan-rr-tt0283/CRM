package com.viswa.crm.service.impl;

import com.viswa.crm.dto.common.TimelineItemDto;
import com.viswa.crm.dto.company.CompanyResponse;
import com.viswa.crm.dto.company.CreateCompanyRequest;
import com.viswa.crm.dto.company.UpdateCompanyRequest;
import com.viswa.crm.dto.company.view.*;
import com.viswa.crm.model.Company;
import com.viswa.crm.repository.ActivityRepository;
import com.viswa.crm.repository.CompanyRepository;
import com.viswa.crm.repository.ContactRepository;
import com.viswa.crm.repository.DealRepository;
import com.viswa.crm.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final DealRepository dealRepository;
    private final ContactRepository contactRepository;
    private final ActivityRepository activityRepository;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        // Uniqueness check
        companyRepository.findByName(request.getName())
                .ifPresent(c -> {
                    throw new RuntimeException("Company with this name already exists");
                });

        Company company = new Company();
        company.setName(request.getName());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setAddress(request.getAddress());
        company.setCreatedAt(LocalDateTime.now());

        Long id = companyRepository.save(company);
        company.setId(id);

        return mapToResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long companyId, UpdateCompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (request.getName() != null && !request.getName().equals(company.getName())) {
            companyRepository.findByName(request.getName())
                    .ifPresent(existing -> {
                        throw new RuntimeException("Another company with this name already exists");
                    });
            company.setName(request.getName());
        }

        if (request.getEmail() != null) {
            company.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }

        companyRepository.update(company);
        return mapToResponse(company);
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId, String roleName) {

        if (!"ADMIN".equals(roleName))
            throw new RuntimeException("Only admin can delete Company");

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // company has deals
        if (dealRepository.existsByCompanyId(companyId))
            throw new RuntimeException("Cannot delete company with existing deals");

        // company has contacts
        if (contactRepository.existsByCompanyId(companyId))
            throw new RuntimeException("Cannot delete company with existing contacts");

        companyRepository.deleteById(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompanies(String keyword) {

        return companyRepository.searchByKeyword(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .phone(company.getPhone())
                .address(company.getAddress())
                .createdAt(company.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyDetail(
            Long companyId,
            Long currentUserId,
            String roleName
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // ===== CONTACTS =====
        List<ContactSummaryDto> contacts =
                contactRepository.findByCompanyId(companyId)
                        .stream()
                        .map(c -> ContactSummaryDto.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .email(c.getEmail())
                                .phone(c.getPhone())
                                .jobTitle(c.getJobTitle())
                                .createdAt(c.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

        // ===== DEALS =====
        List<com.viswa.crm.dto.company.view.DealSummaryDto> deals =
                dealRepository.findByCompanyId(companyId)
                        .stream()
                        .map(d -> DealSummaryDto.builder()
                                .id(d.getId())
                                .title(d.getTitle())
                                .amount(d.getAmount())
                                .status(d.getStatus())
                                .assignedUserName(
                                        d.getAssignedUser() != null
                                                ? d.getAssignedUser().getFullName()
                                                : null
                                )
                                .contactName(
                                        d.getContact() != null
                                                ? d.getContact().getName()
                                                : null
                                )
                                .createdAt(d.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

        // ===== ACTIVITIES =====
        List<com.viswa.crm.dto.company.view.ActivitySummaryDto> activities =
                activityRepository.findByCompanyId(companyId)
                        .stream()
                        .map(a -> ActivitySummaryDto.builder()
                                .id(a.getId())
                                .type(a.getActivityType())
                                .statusCode(a.getStatusCode())
                                .description(a.getDescription())
                                .dueDate(a.getDueDate())
                                .createdAt(a.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

        // ===== TIMELINE =====
        List<TimelineItemDto> timeline = new ArrayList<>();

        contacts.forEach(c ->
                timeline.add(TimelineItemDto.builder()
                        .entityType("CONTACT")
                        .entityId(c.getId())
                        .title(c.getName())
                        .subtitle("Contact created")
                        .createdAt(c.getCreatedAt())
                        .build())
        );

        deals.forEach(d ->
                timeline.add(TimelineItemDto.builder()
                        .entityType("DEAL")
                        .entityId(d.getId())
                        .title(d.getTitle())
                        .subtitle("Deal created")
                        .createdAt(d.getCreatedAt())
                        .build())
        );

        activities.forEach(a ->
                timeline.add(TimelineItemDto.builder()
                        .entityType("ACTIVITY")
                        .entityId(a.getId())
                        .title(a.getDescription())
                        .subtitle(a.getType().name())
                        .createdAt(a.getCreatedAt())
                        .build())
        );

        timeline.sort(
                Comparator.comparing(TimelineItemDto::getCreatedAt).reversed()
        );

        return CompanyDetailResponse.builder()
                .company(
                        CompanyHeaderDto.builder()
                                .id(company.getId())
                                .name(company.getName())
                                .email(company.getEmail())
                                .phone(company.getPhone())
                                .address(company.getAddress())
                                .createdAt(company.getCreatedAt())
                                .build()
                )
                .contacts(contacts)
                .deals(deals)
                .activities(activities)
                .timeline(timeline)
                .build();
    }
}