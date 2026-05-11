package com.campus.activity.model.row;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeedbackSummaryRow {
    private Long feedbackCount;
    private BigDecimal averageRating;
    private Long positiveCount;
    private Long lowRatingCount;
}
