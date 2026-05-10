package com.campus.activity.model.vo;

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

    public static RegistrationActionVO absent(Integer activityId, Integer absentCount) {
        return new RegistrationActionVO(null, activityId, null, null, null, null, absentCount);
    }
}
