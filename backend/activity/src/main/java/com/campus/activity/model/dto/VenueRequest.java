package com.campus.activity.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VenueRequest(@NotBlank String venueName, @NotBlank String roomNumber, @Min(1) int capacity, int campusId) {
}
