package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.model.dto.CheckInRequest;
import com.campus.activity.service.CheckInService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/registrations")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/{registrationId}/check-in-code")
    public Result<Map<String, Object>> code(@PathVariable int registrationId) {
        return Result.success(checkInService.code(registrationId));
    }

    @PatchMapping("/check-in")
    public Result<Map<String, Object>> checkIn(@RequestBody CheckInRequest request) {
        return Result.success(checkInService.checkIn(request));
    }
}
