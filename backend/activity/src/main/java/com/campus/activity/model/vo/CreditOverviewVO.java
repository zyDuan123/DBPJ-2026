package com.campus.activity.model.vo;

import java.util.List;

public record CreditOverviewVO(CreditSummaryVO summary, List<CreditRiskStudentVO> riskStudents) {
}
