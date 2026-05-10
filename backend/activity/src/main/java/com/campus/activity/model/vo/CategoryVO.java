package com.campus.activity.model.vo;

import java.util.Map;

public record CategoryVO(Integer id, String categoryName) {
    public static CategoryVO from(Map<String, Object> row) {
        return new CategoryVO(
                ActivityListItemVO.intValue(row.get("id")),
                ActivityListItemVO.stringValue(row.get("categoryName"))
        );
    }
}
