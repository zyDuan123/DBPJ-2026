package com.campus.activity.model.vo;

import java.util.Map;

public record CampusVO(Integer id, String campusName, String location) {
    public static CampusVO from(Map<String, Object> row) {
        return new CampusVO(
                ActivityListItemVO.intValue(row.get("id")),
                ActivityListItemVO.stringValue(row.get("campusName")),
                ActivityListItemVO.stringValue(row.get("location"))
        );
    }
}
