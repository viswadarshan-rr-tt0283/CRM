package com.viswa.crm.repository;

import com.viswa.crm.model.ActivityStatusDefinition;
import com.viswa.crm.model.ActivityType;

import java.util.List;

public interface ActivityStatusRepository {

    String findDefaultStatus(ActivityType type);

    boolean isTerminal(ActivityType type, String statusCode);

    boolean isValidTransition(
            ActivityType type,
            String current,
            String next
    );
    List<ActivityStatusDefinition> findNextStatuses(
            ActivityType type,
            String current
    );

    String findLabel(ActivityType type, String code);
}
