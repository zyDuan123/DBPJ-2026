package com.campus.activity.model.vo;

import com.campus.activity.model.row.RegistrationListItemRow;

import java.time.LocalDateTime;
import java.util.Map;

public record RegistrationListItemVO(Integer registrationId,
                                     String registrationStatus,
                                     Integer queueNo,
                                     LocalDateTime registrationTime,
                                     LocalDateTime checkInTime,
                                     Integer activityId,
                                     String title,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime,
                                     String campusName,
                                     String venueName,
                                     String roomNumber,
                                     String studentName,
                                     String studentNo,
                                     String phone) {
    public static RegistrationListItemVO from(Map<String, Object> row) {
        return new RegistrationListItemVO(
                ActivityListItemVO.intValue(row.get("registrationId")),
                ActivityListItemVO.stringValue(row.get("registrationStatus")),
                ActivityListItemVO.intValue(row.get("queueNo")),
                ActivityListItemVO.timeValue(row.get("registrationTime")),
                ActivityListItemVO.timeValue(row.get("checkInTime")),
                ActivityListItemVO.intValue(row.get("activityId")),
                ActivityListItemVO.stringValue(row.get("title")),
                ActivityListItemVO.timeValue(row.get("startTime")),
                ActivityListItemVO.timeValue(row.get("endTime")),
                ActivityListItemVO.stringValue(row.get("campusName")),
                ActivityListItemVO.stringValue(row.get("venueName")),
                ActivityListItemVO.stringValue(row.get("roomNumber")),
                ActivityListItemVO.stringValue(row.get("studentName")),
                ActivityListItemVO.stringValue(row.get("studentNo")),
                ActivityListItemVO.stringValue(row.get("phone"))
        );
    }

    public static RegistrationListItemVO from(RegistrationListItemRow row) {
        return new RegistrationListItemVO(
                row.getRegistrationId(),
                row.getRegistrationStatus(),
                row.getQueueNo(),
                row.getRegistrationTime(),
                row.getCheckInTime(),
                row.getActivityId(),
                row.getTitle(),
                row.getStartTime(),
                row.getEndTime(),
                row.getCampusName(),
                row.getVenueName(),
                row.getRoomNumber(),
                row.getStudentName(),
                row.getStudentNo(),
                row.getPhone()
        );
    }
}
