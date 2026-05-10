package com.campus.activity.controller;

import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.model.dto.ActivityRequest;
import com.campus.activity.model.dto.ReviewRequest;
import com.campus.activity.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer campusId,
                                                        @RequestParam(required = false) Integer categoryId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "false") boolean mine) {
        return Result.success(activityService.list(page, size, keyword, campusId, categoryId, status, mine));
    }

    @GetMapping("/{activityId}")
    public Result<Map<String, Object>> detail(@PathVariable int activityId) {
        return Result.success(activityService.detail(activityId));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody ActivityRequest request) {
        return Result.success(activityService.create(request));
    }

    @PutMapping("/{activityId}")
    public Result<Map<String, Object>> update(@PathVariable int activityId, @Valid @RequestBody ActivityRequest request) {
        return Result.success(activityService.update(activityId, request));
    }

    @PostMapping("/{activityId}/submit")
    public Result<Map<String, Object>> submit(@PathVariable int activityId) {
        return Result.success(activityService.submit(activityId));
    }

    @PostMapping("/{activityId}/review")
    public Result<Map<String, Object>> review(@PathVariable int activityId, @Valid @RequestBody ReviewRequest request) {
        return Result.success(activityService.review(activityId, request));
    }

    @PostMapping("/{activityId}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable int activityId) {
        return Result.success(activityService.cancel(activityId));
    }
}
