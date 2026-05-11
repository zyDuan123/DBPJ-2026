package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckInTargetRow {
    private Integer registrationId;
    private Integer studentId;
    private String status;
    private LocalDateTime endTime;
    private Integer organizerId;
}
