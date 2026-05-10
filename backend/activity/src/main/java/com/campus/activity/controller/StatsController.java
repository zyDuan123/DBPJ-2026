package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.overview());
    }

    @GetMapping("/campus-usage")
    public Result<List<Map<String, Object>>> campusUsage() {
        return Result.success(statsService.campusUsage());
    }

    @GetMapping("/category-popularity")
    public Result<List<Map<String, Object>>> categoryPopularity() {
        return Result.success(statsService.categoryPopularity());
    }
}
