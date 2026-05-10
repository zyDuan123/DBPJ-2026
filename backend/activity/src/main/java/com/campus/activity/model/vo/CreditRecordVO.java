package com.campus.activity.model.vo;

import java.time.LocalDateTime;
import java.util.Map;

public record CreditRecordVO(Integer recordId,
                             Integer changeValue,
                             String reasonType,
                             String reason,
                             LocalDateTime createdAt,
                             Integer activityId,
                             String activityTitle) {
    public static CreditRecordVO from(Map<String, Object> row) {
        return new CreditRecordVO(
                ActivityListItemVO.intValue(row.get("recordId")),
                ActivityListItemVO.intValue(row.get("changeValue")),
                ActivityListItemVO.stringValue(row.get("reasonType")),
                ActivityListItemVO.stringValue(row.get("reason")),
                ActivityListItemVO.timeValue(row.get("createdAt")),
                ActivityListItemVO.intValue(row.get("activityId")),
                ActivityListItemVO.stringValue(row.get("activityTitle"))
        );
    }
}
