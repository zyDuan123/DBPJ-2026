package com.campus.activity.model.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackRecordRow {
    private Integer feedbackId;
    private String studentName;
    private String studentNo;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
