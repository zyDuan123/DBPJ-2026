package com.campus.activity.model.row;

import lombok.Data;

@Data
public class RegistrationLockRow {
    private Integer registrationId;
    private Integer studentId;
    private Integer activityId;
    private String status;
}
