package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationListItemRow {
    private Integer registrationId;
    private String registrationStatus;
    private Integer queueNo;
    private LocalDateTime registrationTime;
    private LocalDateTime checkInTime;
    private Integer activityId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String campusName;
    private String venueName;
    private String roomNumber;
    private String studentName;
    private String studentNo;
    private String phone;
}
