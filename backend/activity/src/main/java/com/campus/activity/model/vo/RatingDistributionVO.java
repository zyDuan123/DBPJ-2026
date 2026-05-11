package com.campus.activity.model.vo;

import com.campus.activity.model.row.RatingCountRow;

import java.util.Map;

public record RatingDistributionVO(Integer rating, Long count, Integer rate) {
    public static RatingDistributionVO from(Map<String, Object> row) {
        return new RatingDistributionVO(
                ActivityListItemVO.intValue(row.get("rating")),
                StatsOverviewVO.longValue(row.get("count")),
                ActivityListItemVO.intValue(row.get("rate"))
        );
    }

    public static RatingDistributionVO from(RatingCountRow row, long total) {
        long count = row.getCount() == null ? 0 : row.getCount();
        int rate = total == 0 ? 0 : Math.round((count * 100f) / total);
        return new RatingDistributionVO(row.getRating(), count, rate);
    }

    public static RatingDistributionVO empty(int rating) {
        return new RatingDistributionVO(rating, 0L, 0);
    }
}
