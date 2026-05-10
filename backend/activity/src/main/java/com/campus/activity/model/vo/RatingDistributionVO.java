package com.campus.activity.model.vo;

import java.util.Map;

public record RatingDistributionVO(Integer rating, Long count, Integer rate) {
    public static RatingDistributionVO from(Map<String, Object> row) {
        return new RatingDistributionVO(
                ActivityListItemVO.intValue(row.get("rating")),
                StatsOverviewVO.longValue(row.get("count")),
                ActivityListItemVO.intValue(row.get("rate"))
        );
    }
}
