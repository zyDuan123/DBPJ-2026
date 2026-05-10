package com.campus.activity.model.vo;

import com.campus.activity.common.TimeValues;

import java.time.LocalDateTime;
import java.util.Map;

public record ActivityListItemVO(Integer id,
                                 String title,
                                 String posterUrl,
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
                                 String organizerName) {
    public static ActivityListItemVO from(Map<String, Object> row) {
        return new ActivityListItemVO(
                intValue(row.get("id")),
                stringValue(row.get("title")),
                stringValue(row.get("posterUrl")),
                timeValue(row.get("startTime")),
                timeValue(row.get("endTime")),
                timeValue(row.get("enrollDeadline")),
                stringValue(row.get("campusName")),
                stringValue(row.get("venueName")),
                stringValue(row.get("roomNumber")),
                stringValue(row.get("categoryName")),
                intValue(row.get("venueId")),
                intValue(row.get("categoryId")),
                intValue(row.get("capacityLimit")),
                intValue(row.get("currentEnrollment")),
                stringValue(row.get("status")),
                stringValue(row.get("rejectReason")),
                stringValue(row.get("organizerName"))
        );
    }

    static Integer intValue(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    static LocalDateTime timeValue(Object value) {
        return value == null ? null : TimeValues.toLocalDateTime(value);
    }
}
