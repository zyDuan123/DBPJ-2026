package com.campus.activity.model.vo;

import java.math.BigDecimal;
import java.util.Map;

public record CategoryPopularityVO(Integer categoryId,
                                   String categoryName,
                                   Long activityCount,
                                   BigDecimal averageEnrollment) {
    public static CategoryPopularityVO from(Map<String, Object> row) {
        return new CategoryPopularityVO(
                ActivityListItemVO.intValue(row.get("categoryId")),
                ActivityListItemVO.stringValue(row.get("categoryName")),
                StatsOverviewVO.longValue(row.get("activityCount")),
                decimalValue(row.get("averageEnrollment"))
        );
    }

    static BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
