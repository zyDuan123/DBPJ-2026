package com.campus.activity.model.vo;

import java.util.Map;

public record StatsOverviewVO(Long activityCount,
                              Long pendingReviewCount,
                              Long publishedCount,
                              Long registrationCount,
                              Long checkedInCount) {
    public static StatsOverviewVO from(Map<String, Object> row) {
        return new StatsOverviewVO(
                longValue(row.get("activityCount")),
                longValue(row.get("pendingReviewCount")),
                longValue(row.get("publishedCount")),
                longValue(row.get("registrationCount")),
                longValue(row.get("checkedInCount"))
        );
    }

    static Long longValue(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
