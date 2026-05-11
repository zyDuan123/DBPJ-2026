package com.campus.activity.model.vo;

import com.campus.activity.model.row.RegistrationActionRow;

import java.util.Map;

public record RegistrationActionVO(Integer registrationId,
                                   Integer activityId,
                                   String registrationStatus,
                                   Integer queueNo,
                                   String message,
                                   Integer promotedRegistrationId,
                                   Integer absentCount) {
    public static RegistrationActionVO from(Map<String, Object> row) {
        return new RegistrationActionVO(
                ActivityListItemVO.intValue(row.get("registrationId")),
                ActivityListItemVO.intValue(row.get("activityId")),
                ActivityListItemVO.stringValue(row.get("registrationStatus")),
                ActivityListItemVO.intValue(row.get("queueNo")),
                ActivityListItemVO.stringValue(row.get("message")),
                ActivityListItemVO.intValue(row.get("promotedRegistrationId")),
                ActivityListItemVO.intValue(row.get("absentCount"))
        );
    }

    public static RegistrationActionVO from(RegistrationActionRow row, String message) {
        return new RegistrationActionVO(
                row.getRegistrationId(),
                row.getActivityId(),
                row.getRegistrationStatus(),
                row.getQueueNo(),
                message,
                null,
                null
        );
    }

    public static RegistrationActionVO status(Integer registrationId, String registrationStatus) {
        return new RegistrationActionVO(registrationId, null, registrationStatus, null, null, null, null);
    }

    public static RegistrationActionVO cancelled(Integer registrationId, Integer promotedRegistrationId) {
        return new RegistrationActionVO(registrationId, null, "CANCELLED", null, null, promotedRegistrationId, null);
    }

    public static RegistrationActionVO enrollment(Integer registrationId, Integer activityId,
                                                  String registrationStatus, Integer queueNo, String message) {
        return new RegistrationActionVO(registrationId, activityId, registrationStatus, queueNo, message, null, null);
    }

    public static RegistrationActionVO absent(Integer activityId, Integer absentCount) {
        return new RegistrationActionVO(null, activityId, null, null, null, null, absentCount);
    }
}
