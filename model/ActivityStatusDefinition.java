package com.viswa.crm.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityStatusDefinition {

    private String activityType;   // TASK, EMAIL, CALL...
    private String code;           // DRAFT, SENT, DONE...
    private String displayName;    // UI label
    private boolean terminal;
    private int sortOrder;
}
