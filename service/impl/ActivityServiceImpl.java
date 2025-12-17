package com.viswa.crm.service.impl;

import com.viswa.crm.dto.activity.ActivityResponse;
import com.viswa.crm.dto.activity.CreateActivityRequest;
import com.viswa.crm.dto.activity.UpdateActivityRequest;
import com.viswa.crm.dto.activity.view.*;
import com.viswa.crm.model.*;
import com.viswa.crm.repository.ActivityRepository;
import com.viswa.crm.repository.DealRepository;
import com.viswa.crm.repository.UserRepository;
import com.viswa.crm.service.ActivityService;
import com.viswa.crm.repository.ActivityStatusRepository;
import com.viswa.crm.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final DealRepository dealRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId,
            String currentUserRole
    ) {

        if (!permissionService.hasPermission(currentUserId, "ACTIVITY_CREATE")) {
            throw new RuntimeException("Access denied");
        }

        List<Long> visibleDealIds = resolveVisibleDealIds(currentUserId);

        if (!visibleDealIds.contains(request.getDealId())) {
            throw new RuntimeException("Access denied");
        }

        Deal deal = dealRepository.findById(request.getDealId())
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        validateDueDate(request.getDueDate());

        Activity activity = new Activity();
        activity.setDeal(deal);
        activity.setOwnerUserId(currentUserId);
        activity.setActivityType(request.getType());
        activity.setStatusCode(defaultStatusFor(request.getType()));
        activity.setDescription(request.getDescription());
        activity.setDueDate(request.getDueDate());
        activity.setCreatedAt(LocalDateTime.now());

        Long id = activityRepository.save(activity);
        activity.setId(id);

        return mapToResponse(activity);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(
            Long activityId,
            UpdateActivityRequest request,
            Long currentUserId,
            String currentUserRole
    ) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        enforceActivityAccess(activity, currentUserId, "ACTIVITY_UPDATE");

        if (isTerminalState(activity)) {
            throw new RuntimeException("Completed activity cannot be modified");
        }

        if (request.getType() != null) {
            activity.setActivityType(request.getType());
        }

        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }

        if (request.getDueDate() != null) {
            validateDueDate(request.getDueDate());
            activity.setDueDate(request.getDueDate());
        }

        if (request.getStatusCode() != null) {

            validateStatusTransition(activity, request.getStatusCode());

            activity.setStatusCode(request.getStatusCode());
        }


        activityRepository.update(activity);
        return mapToResponse(activity);
    }

    @Override
    @Transactional
    public void deleteActivity(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    ) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        enforceActivityAccess(activity, currentUserId, "ACTIVITY_DELETE");
        activityRepository.deleteById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    ) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        enforceActivityAccess(activity, currentUserId, "ACTIVITY_VIEW_SELF");
        return mapToResponse(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivities(int limit) {

        List<Activity> activities = activityRepository.findRecentActivities(limit);

        List<Long> dealIds = activities.stream()
                .map(a -> a.getDeal().getId())
                .distinct()
                .collect(toList());

        Map<Long, String> dealTitleMap =
                dealRepository.findByIds(dealIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Deal::getId,
                                Deal::getTitle
                        ));

        return activities.stream()
                .map(a -> ActivityResponse.builder()
                        .id(a.getId())
                        .dealId(a.getDeal().getId())
                        .dealTitle(dealTitleMap.get(a.getDeal().getId()))
                        .type(a.getActivityType())
                        .statusCode(a.getStatusCode())
                        .description(a.getDescription())
                        .dueDate(a.getDueDate())
                        .createdAt(a.getCreatedAt())
                        .build()
                )
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesForUser(
            Long currentUserId,
            String currentUserRole,
            int limit
    ) {

        List<Long> visibleDealIds = resolveVisibleDealIds(currentUserId);

        List<Activity> activities = activityRepository.findRecentByDealIds(visibleDealIds, limit);

        Map<Long, String> dealTitleMap = loadDealTitles(activities);

        return activities.stream()
                .map(a -> mapToResponses(a, dealTitleMap))
                .collect(toList());
    }

    private ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .dealId(activity.getDeal().getId())
                .dealTitle(activity.getDeal().getTitle())
                .type(activity.getActivityType())
                .statusCode(activity.getStatusCode())
                .description(activity.getDescription())
                .dueDate(activity.getDueDate())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private ActivityResponse mapToResponses(
            Activity activity,
            Map<Long, String> dealTitleMap
    ) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .dealId(activity.getDeal().getId())
                .dealTitle(dealTitleMap.get(activity.getDeal().getId()))
                .type(activity.getActivityType())
                .statusCode(activity.getStatusCode())
                .description(activity.getDescription())
                .dueDate(activity.getDueDate())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private void validateDueDate(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Due date cannot be in the past");
        }
    }

    private boolean isTerminalState(Activity activity) {
        return activityStatusRepository.isTerminal(
                activity.getActivityType(),
                activity.getStatusCode()
        );
    }

    private String defaultStatusFor(ActivityType type) {
        String defaultStatus; // Declare the result variable

        switch (type) {
            case TASK:
                defaultStatus = "PENDING";
                break;
            case CALL:
                defaultStatus = "SCHEDULED";
                break;
            case MEETING:
                defaultStatus = "SCHEDULED";
                break;
            case EMAIL:
                defaultStatus = "DRAFT";
                break;
            case LETTER:
                defaultStatus = "DRAFT";
                break;
            case SOCIAL_MEDIA:
                defaultStatus = "DRAFT";
                break;
            default:
                // Handle any unexpected or unlisted ActivityType
                defaultStatus = "PENDING";
                break;
        }
        return defaultStatus;
    }
    private void validateStatusTransition(
            Activity activity,
            String nextStatusCode
    ) {

        boolean valid = activityStatusRepository.isValidTransition(
                activity.getActivityType(),
                activity.getStatusCode(),
                nextStatusCode
        );

        if (!valid) {
            throw new RuntimeException("Invalid status for activity type");
        }
    }

    public ActivityResponse changeStatus(
            Long activityId,
            String nextStatus,
            Long userId,
            String role
    ) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        enforceActivityAccess(activity, userId, "ACTIVITY_CHANGE_STATUS");

        if (!activityStatusRepository.isValidTransition(
                activity.getActivityType(),
                activity.getStatusCode(),
                nextStatus
        )) {
            throw new RuntimeException("Invalid status transition");
        }

        activity.setStatusCode(nextStatus);
        activityRepository.update(activity);

        return mapToResponse(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatusOptions(
            ActivityType type,
            String currentStatusCode
    ) {

        List<ActivityStatusDefinition> next =
                activityStatusRepository.findNextStatuses(
                        type,
                        currentStatusCode
                );

        boolean terminal =
                activityStatusRepository.isTerminal(type, currentStatusCode);

        Map<String, Object> result = new HashMap<>();
        result.put("current", currentStatusCode);
        result.put("terminal", terminal);
        result.put(
                "allowedNext",
                next.stream()
                        .map(s -> Map.of(
                                "code", s.getCode(),
                                "label", s.getDisplayName()
                        ))
                        .collect(Collectors.toList())
        );

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityViewResponse getActivityView(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    ) {

        // 1. Load activity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        enforceActivityAccess(activity, currentUserId, "ACTIVITY_VIEW_SELF");

        // 2. Load deal
        Deal deal = dealRepository.findById(activity.getDeal().getId())
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        // 3. Load company
        Company company = deal.getCompany();

        // 4. Load contacts for this deal
        List<Contact> contacts =
                dealRepository.findContactsByDealId(deal.getId());

        // 5. Status options
        Map<String, Object> statusOptions =
                getStatusOptions(
                        activity.getActivityType(),
                        activity.getStatusCode()
                );

        return ActivityViewResponse.builder()
                .activity(mapActivityDetail(activity))
                .deal(mapDealSummary(deal))
                .company(mapCompanySummary(company))
                .contacts(
                        contacts.stream()
                                .map(this::mapContactSummary)
                                .collect(Collectors.toList())
                )
                .statusOptions(mapStatusOptions(statusOptions))
                .build();
    }
    private ActivityDetailDto mapActivityDetail(Activity a) {
        return ActivityDetailDto.builder()
                .id(a.getId())
                .type(a.getActivityType())
                .statusCode(a.getStatusCode())
                .statusLabel(
                        activityStatusRepository
                                .findLabel(a.getActivityType(), a.getStatusCode())
                )
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .createdAt(a.getCreatedAt())
                .ownerUserId(a.getOwnerUserId())
                .build();
    }

    private DealSummaryDto mapDealSummary(Deal d) {
        return DealSummaryDto.builder()
                .id(d.getId())
                .title(d.getTitle())
                .amount(d.getAmount())
                .status(d.getStatus())
                .companyId(d.getCompany().getId())
                .build();
    }

    private CompanySummaryDto mapCompanySummary(Company c) {
        return CompanySummaryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .build();
    }

    private ContactSummaryDto mapContactSummary(Contact c) {
        return ContactSummaryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .jobTitle(c.getJobTitle())
                .build();
    }

    private ActivityStatusOptionsDto mapStatusOptions(Map<String, Object> raw) {
        return ActivityStatusOptionsDto.builder()
                .current((String) raw.get("current"))
                .terminal((Boolean) raw.get("terminal"))
                .allowedNext(
                        ((List<Map<String, String>>) raw.get("allowedNext"))
                                .stream()
                                .map(m -> NextStatusDto.builder()
                                        .code(m.get("code"))
                                        .label(m.get("label"))
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    private Map<Long, String> loadDealTitles(List<Activity> activities) {

        List<Long> dealIds = activities.stream()
                .map(a -> a.getDeal().getId())
                .distinct()
                .collect(Collectors.toList());

        return dealRepository.findByIds(dealIds)
                .stream()
                .collect(Collectors.toMap(
                        Deal::getId,
                        Deal::getTitle
                ));
    }
    private List<Long> resolveVisibleDealIds(Long currentUserId) {

        // 1. Full visibility
        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_ALL")) {
            return dealRepository.findAllDealIds();
        }

        // 2. Team visibility
        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_TEAM")) {

            List<Long> teamUserIds =
                    userRepository.findUserIdsByManagerId(currentUserId);

            teamUserIds.add(currentUserId);

            return dealRepository.findDealIdsByUserIds(teamUserIds);
        }

        // 3. Self visibility
        if (permissionService.hasPermission(currentUserId, "DEAL_VIEW_SELF")) {
            return dealRepository.findDealIdsByUserId(currentUserId);
        }

        // 4. Default deny
        return List.of();
    }

    private void enforceActivityAccess(
            Activity activity,
            Long userId,
            String requiredPermission
    ) {

        // 1. Permission check
        if (!permissionService.hasPermission(userId, requiredPermission)) {
            throw new RuntimeException("Access denied");
        }

        // 2. Deal visibility check
        List<Long> visibleDealIds = resolveVisibleDealIds(userId);

        if (!visibleDealIds.contains(activity.getDeal().getId())) {
            throw new RuntimeException("Access denied");
        }
    }

}
