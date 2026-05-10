package com.campus.activity.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ActivityRequest(@NotBlank String title,
                              int venueId,
                              int categoryId,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              LocalDateTime enrollDeadline,
                              @Min(1) int capacityLimit,
                              String posterUrl,
                              String description) {
}
