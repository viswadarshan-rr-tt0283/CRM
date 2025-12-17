package com.viswa.crm.service;

import com.viswa.crm.dto.deal.ChangeDealStatusRequest;
import com.viswa.crm.dto.deal.CreateDealRequest;
import com.viswa.crm.dto.deal.DealResponse;
import com.viswa.crm.dto.deal.UpdateDealRequest;
import com.viswa.crm.dto.deal.view.DealDetailResponse;
import com.viswa.crm.model.DealStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

public interface DealService {

//    DealResponse createDeal(CreateDealRequest request);

//    DealResponse updateDeal(Long dealId, UpdateDealRequest request);

//    void deleteDeal(Long dealId);

//    DealResponse getDealById(Long dealId);

//    List<DealResponse> getDealsByUser(Long userId);

//    List<DealResponse> getDealsByCompany(Long companyId);

//    List<DealResponse> searchDeals(String keyword);

//    DealResponse changeDealStatus(Long dealId, ChangeDealStatusRequest request);

//    Map<DealStatus, Long> getDealCountByStatus();


    // recently added

    List<DealResponse> getDealsForUser(
            Long currentUserId,
            String roleName,
            String keyword
    );

    DealResponse getDealById(
            Long dealId,
            Long currentUserId,
            String roleName
    );

    DealResponse createDeal(
            CreateDealRequest request,
            Long currentUserId,
            String roleName
    );

    DealResponse updateDeal(
            Long dealId,
            UpdateDealRequest request,
            Long currentUserId,
            String roleName
    );

    void deleteDeal(
            Long dealId,
            Long currentUserId,
            String roleName
    );

    DealResponse changeDealStatus(
            Long dealId,
            ChangeDealStatusRequest request,
            Long currentUserId,
            String roleName
    );

    List<DealResponse> getDealsForAdmin(String keyword);

    List<DealResponse> getDealsForManager(Long managerId, String keyword);

    List<DealResponse> getDealsForSales(Long userId, String keyword);

    DealDetailResponse getDealDetail(
            Long dealId,
            Long currentUserId,
            String roleName
    );

}
