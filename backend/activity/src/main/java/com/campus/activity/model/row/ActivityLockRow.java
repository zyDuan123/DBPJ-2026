package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLockRow {
    private Integer activityId;
    private String status;
    private Integer currentEnrollment;
    private Integer capacityLimit;
    private LocalDateTime enrollDeadline;
    private LocalDateTime endTime;
}
