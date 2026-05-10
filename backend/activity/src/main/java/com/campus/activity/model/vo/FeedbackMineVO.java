package com.campus.activity.model.vo;

import java.time.LocalDateTime;
import java.util.Map;

public record FeedbackMineVO(Integer feedbackId,
                             Integer rating,
                             String content,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
    public static FeedbackMineVO from(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        return new FeedbackMineVO(
                ActivityListItemVO.intValue(row.get("feedbackId")),
                ActivityListItemVO.intValue(row.get("rating")),
                ActivityListItemVO.stringValue(row.get("content")),
                ActivityListItemVO.timeValue(row.get("createdAt")),
                ActivityListItemVO.timeValue(row.get("updatedAt"))
        );
    }
}
