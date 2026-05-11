package com.campus.activity.model.vo;

import com.campus.activity.model.row.FeedbackSummaryRow;

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

    public static FeedbackSummaryVO from(FeedbackSummaryRow row) {
        long feedbackCount = row.getFeedbackCount() == null ? 0 : row.getFeedbackCount();
        BigDecimal averageRating = row.getAverageRating() == null ? BigDecimal.ZERO : row.getAverageRating();
        long positiveCount = row.getPositiveCount() == null ? 0 : row.getPositiveCount();
        long lowRatingCount = row.getLowRatingCount() == null ? 0 : row.getLowRatingCount();
        int positiveRate = feedbackCount == 0 ? 0 : Math.round((positiveCount * 100f) / feedbackCount);
        return new FeedbackSummaryVO(feedbackCount, averageRating, positiveRate, lowRatingCount);
    }
}
