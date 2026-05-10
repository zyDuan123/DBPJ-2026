package com.campus.activity.model.vo;

import java.util.Map;

public record CreditSummaryVO(Long recordCount, Long totalChange, Long absentCount, Long checkInCreditCount) {
    public static CreditSummaryVO from(Map<String, Object> row) {
        return new CreditSummaryVO(
                StatsOverviewVO.longValue(row.get("recordCount")),
                StatsOverviewVO.longValue(row.get("totalChange")),
                StatsOverviewVO.longValue(row.get("absentCount")),
                StatsOverviewVO.longValue(row.get("checkInCreditCount"))
        );
    }
}
