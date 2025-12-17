package com.viswa.crm.service;

import com.viswa.crm.dto.activity.ActivityResponse;
import com.viswa.crm.dto.activity.CreateActivityRequest;
import com.viswa.crm.dto.activity.UpdateActivityRequest;
import com.viswa.crm.dto.activity.view.ActivityViewResponse;
import com.viswa.crm.model.ActivityType;

import java.util.List;
import java.util.Map;

public interface ActivityService {

    ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId,
            String currentUserRole
    );

    ActivityResponse updateActivity(
            Long activityId,
            UpdateActivityRequest request,
            Long currentUserId,
            String currentUserRole
    );

    void deleteActivity(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    );

//    ActivityResponse markAsDone(
//            Long activityId,
//            Long currentUserId,
//            String currentUserRole
//    );

    ActivityResponse getActivityById(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    );

    List<ActivityResponse> getActivitiesForUser(
            Long currentUserId,
            String currentUserRole,
            int limit
    );

//    List<ActivityResponse> getActivitiesByDealId(
//            Long dealId,
//            Long currentUserId,
//            String currentUserRole
//    );

    List<ActivityResponse> getRecentActivities(int limit);

    ActivityResponse changeStatus(
            Long activityId,
            String nextStatus,
            Long userId,
            String role
    );

    Map<String, Object> getStatusOptions(
            ActivityType type,
            String currentStatusCode
    );
    ActivityViewResponse getActivityView(
            Long activityId,
            Long currentUserId,
            String currentUserRole
    );

}
