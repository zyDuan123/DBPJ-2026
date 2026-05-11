package com.campus.activity.model.row;

import lombok.Data;

@Data
public class RegistrationActionRow {
    private Integer registrationId;
    private Integer activityId;
    private String registrationStatus;
    private Integer queueNo;
}
