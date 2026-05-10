package com.campus.activity.model.vo;

import java.util.Map;

public record KeywordVO(String keyword, Integer count) {
    public static KeywordVO from(Map<String, Object> row) {
        return new KeywordVO(
                ActivityListItemVO.stringValue(row.get("keyword")),
                ActivityListItemVO.intValue(row.get("count"))
        );
    }
}
