package com.campus.activity.model.vo;

import java.util.Map;

public record CreditRiskStudentVO(Integer studentId,
                                  String studentName,
                                  String studentNo,
                                  Long creditScore,
                                  Long absentCount) {
    public static CreditRiskStudentVO from(Map<String, Object> row) {
        return new CreditRiskStudentVO(
                ActivityListItemVO.intValue(row.get("studentId")),
                ActivityListItemVO.stringValue(row.get("studentName")),
                ActivityListItemVO.stringValue(row.get("studentNo")),
                StatsOverviewVO.longValue(row.get("creditScore")),
                StatsOverviewVO.longValue(row.get("absentCount"))
        );
    }
}
