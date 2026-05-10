package com.campus.activity.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String categoryName) {
}
