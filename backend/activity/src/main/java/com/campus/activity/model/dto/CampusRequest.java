package com.campus.activity.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CampusRequest(@NotBlank String campusName, String location) {
}
