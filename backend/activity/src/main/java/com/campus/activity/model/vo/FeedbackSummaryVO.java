package com.campus.activity.model.vo;

import java.math.BigDecimal;
import java.util.Map;

public record FeedbackSummaryVO(Long feedbackCount,
                                BigDecimal averageRating,
                                Integer positiveRate,
                                Long lowRatingCount) {
    public static FeedbackSummaryVO from(Map<String, Object> row) {
        return new FeedbackSummaryVO(
                StatsOverviewVO.longValue(row.get("feedbackCount")),
                CategoryPopularityVO.decimalValue(row.get("averageRating")),
                ActivityListItemVO.intValue(row.get("positiveRate")),
                StatsOverviewVO.longValue(row.get("lowRatingCount"))
        );
    }
}
