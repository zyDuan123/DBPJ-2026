package com.campus.activity.model.vo;

import com.campus.activity.model.row.ActivityDetailRow;
import com.campus.activity.model.row.StudentRegistrationRow;

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

    public static ActivityDetailVO from(ActivityDetailRow row, StudentRegistrationRow registration) {
        return new ActivityDetailVO(
                row.getId(),
                row.getTitle(),
                row.getPosterUrl(),
                row.getDescription(),
                row.getStartTime(),
                row.getEndTime(),
                row.getEnrollDeadline(),
                row.getCampusName(),
                row.getVenueName(),
                row.getRoomNumber(),
                row.getCategoryName(),
                row.getVenueId(),
                row.getCategoryId(),
                row.getCapacityLimit(),
                row.getCurrentEnrollment(),
                row.getStatus(),
                row.getRejectReason(),
                row.getOrganizerName(),
                row.getOrganizerId(),
                registration == null ? null : registration.getRegistrationId(),
                registration == null ? null : registration.getRegistrationStatus()
        );
    }
}
