package com.campus.activity.model.vo;

import java.time.LocalDateTime;
import java.util.Map;

public record FeedbackRecordVO(Integer feedbackId,
                               String studentName,
                               String studentNo,
                               Integer rating,
                               String content,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
    public static FeedbackRecordVO from(Map<String, Object> row) {
        return new FeedbackRecordVO(
                ActivityListItemVO.intValue(row.get("feedbackId")),
                ActivityListItemVO.stringValue(row.get("studentName")),
                ActivityListItemVO.stringValue(row.get("studentNo")),
                ActivityListItemVO.intValue(row.get("rating")),
                ActivityListItemVO.stringValue(row.get("content")),
                ActivityListItemVO.timeValue(row.get("createdAt")),
                ActivityListItemVO.timeValue(row.get("updatedAt"))
        );
    }
}
