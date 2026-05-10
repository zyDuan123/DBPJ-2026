package com.campus.activity.model.vo;

import java.util.Map;

public record VenueVO(Integer id,
                      String venueName,
                      String roomNumber,
                      Integer capacity,
                      Integer campusId,
                      String campusName) {
    public static VenueVO from(Map<String, Object> row) {
        return new VenueVO(
                ActivityListItemVO.intValue(row.get("id")),
                ActivityListItemVO.stringValue(row.get("venueName")),
                ActivityListItemVO.stringValue(row.get("roomNumber")),
                ActivityListItemVO.intValue(row.get("capacity")),
                ActivityListItemVO.intValue(row.get("campusId")),
                ActivityListItemVO.stringValue(row.get("campusName"))
        );
    }
}
