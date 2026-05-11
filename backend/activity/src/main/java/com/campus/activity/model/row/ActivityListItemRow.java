package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityListItemRow {
    private Integer id;
    private String title;
    private String posterUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime enrollDeadline;
    private String campusName;
    private String venueName;
    private String roomNumber;
    private String categoryName;
    private Integer venueId;
    private Integer categoryId;
    private Integer capacityLimit;
    private Integer currentEnrollment;
    private String status;
    private String rejectReason;
    private String organizerName;
}
