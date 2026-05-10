package com.campus.activity.model.vo;

import java.time.LocalDateTime;
import java.util.Map;

public record ActivityDetailVO(Integer id,
                               String title,
                               String posterUrl,
                               String description,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               LocalDateTime enrollDeadline,
                               String campusName,
                               String venueName,
                               String roomNumber,
                               String categoryName,
                               Integer venueId,
                               Integer categoryId,
                               Integer capacityLimit,
                               Integer currentEnrollment,
                               String status,
                               String rejectReason,
                               String organizerName,
                               Integer organizerId,
                               Integer registrationId,
                               String registrationStatus) {
    public static ActivityDetailVO from(Map<String, Object> row) {
        return new ActivityDetailVO(
                ActivityListItemVO.intValue(row.get("id")),
                ActivityListItemVO.stringValue(row.get("title")),
                ActivityListItemVO.stringValue(row.get("posterUrl")),
                ActivityListItemVO.stringValue(row.get("description")),
                ActivityListItemVO.timeValue(row.get("startTime")),
                ActivityListItemVO.timeValue(row.get("endTime")),
                ActivityListItemVO.timeValue(row.get("enrollDeadline")),
                ActivityListItemVO.stringValue(row.get("campusName")),
                ActivityListItemVO.stringValue(row.get("venueName")),
                ActivityListItemVO.stringValue(row.get("roomNumber")),
                ActivityListItemVO.stringValue(row.get("categoryName")),
                ActivityListItemVO.intValue(row.get("venueId")),
                ActivityListItemVO.intValue(row.get("categoryId")),
                ActivityListItemVO.intValue(row.get("capacityLimit")),
                ActivityListItemVO.intValue(row.get("currentEnrollment")),
                ActivityListItemVO.stringValue(row.get("status")),
                ActivityListItemVO.stringValue(row.get("rejectReason")),
                ActivityListItemVO.stringValue(row.get("organizerName")),
                ActivityListItemVO.intValue(row.get("organizerId")),
                ActivityListItemVO.intValue(row.get("registrationId")),
                ActivityListItemVO.stringValue(row.get("registrationStatus"))
        );
    }
}
