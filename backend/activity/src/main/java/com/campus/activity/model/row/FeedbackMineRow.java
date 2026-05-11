package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackMineRow {
    private Integer feedbackId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
