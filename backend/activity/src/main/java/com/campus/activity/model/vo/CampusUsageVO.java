package com.campus.activity.model.vo;

import java.util.Map;

public record CampusUsageVO(Integer campusId, String campusName, Long activityCount, Long venueCount) {
    public static CampusUsageVO from(Map<String, Object> row) {
        return new CampusUsageVO(
                ActivityListItemVO.intValue(row.get("campusId")),
                ActivityListItemVO.stringValue(row.get("campusName")),
                StatsOverviewVO.longValue(row.get("activityCount")),
                StatsOverviewVO.longValue(row.get("venueCount"))
        );
    }
}
