package com.campus.activity.model.vo;

import com.campus.activity.model.row.FeedbackTopActivityRow;

import java.math.BigDecimal;
import java.util.Map;

public record FeedbackTopActivityVO(Integer activityId,
                                    String title,
                                    Long feedbackCount,
                                    BigDecimal averageRating) {
    public static FeedbackTopActivityVO from(Map<String, Object> row) {
        return new FeedbackTopActivityVO(
                ActivityListItemVO.intValue(row.get("activityId")),
                ActivityListItemVO.stringValue(row.get("title")),
                StatsOverviewVO.longValue(row.get("feedbackCount")),
                CategoryPopularityVO.decimalValue(row.get("averageRating"))
        );
    }

    public static FeedbackTopActivityVO from(FeedbackTopActivityRow row) {
        return new FeedbackTopActivityVO(
                row.getActivityId(),
                row.getTitle(),
                row.getFeedbackCount(),
                row.getAverageRating()
        );
    }
}
