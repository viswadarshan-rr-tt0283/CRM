package com.viswa.crm.dto.activity;

import com.viswa.crm.model.ActivityStatus;
import com.viswa.crm.model.ActivityType;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

@Data
public class UpdateActivityRequest {

    private ActivityType type;

    private String statusCode;

    private String description;

    @FutureOrPresent
    private LocalDate dueDate;
}
