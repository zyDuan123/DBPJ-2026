package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.model.dto.FeedbackRequest;
import com.campus.activity.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/activities/{activityId}/feedback")
    public Result<Map<String, Object>> submit(@PathVariable int activityId, @Valid @RequestBody FeedbackRequest request) {
        return Result.success(feedbackService.submit(activityId, request));
    }

    @GetMapping("/activities/{activityId}/feedback/my")
    public Result<Map<String, Object>> my(@PathVariable int activityId) {
        return Result.success(feedbackService.my(activityId));
    }

    @GetMapping("/activities/{activityId}/feedback")
    public Result<Map<String, Object>> activityFeedback(@PathVariable int activityId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(defaultValue = "false") boolean lowRatingOnly) {
        return Result.success(feedbackService.activityFeedback(activityId, page, size, lowRatingOnly));
    }

    @GetMapping("/feedback/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(feedbackService.overview());
    }
}
