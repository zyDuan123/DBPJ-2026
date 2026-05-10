package com.campus.activity.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(@NotBlank String result, String reason) {
}
