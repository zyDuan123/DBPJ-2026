package com.campus.activity.model.row;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeedbackTopActivityRow {
    private Integer activityId;
    private String title;
    private Long feedbackCount;
    private BigDecimal averageRating;
}
