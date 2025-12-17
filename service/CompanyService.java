package com.viswa.crm.service;

import com.viswa.crm.dto.company.CompanyResponse;
import com.viswa.crm.dto.company.CreateCompanyRequest;
import com.viswa.crm.dto.company.UpdateCompanyRequest;
import com.viswa.crm.dto.company.view.CompanyDetailResponse;

import java.util.List;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse updateCompany(Long companyId, UpdateCompanyRequest request);

    void deleteCompany(Long companyId, String roleName);

    CompanyResponse getCompanyById(Long companyId);

    List<CompanyResponse> searchCompanies(String keyword);

    List<CompanyResponse> getAllCompanies();

    CompanyDetailResponse getCompanyDetail(
            Long companyId,
            Long currentUserId,
            String roleName
    );

}
