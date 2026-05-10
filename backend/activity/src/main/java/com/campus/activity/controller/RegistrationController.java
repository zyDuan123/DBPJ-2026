package com.campus.activity.controller;

import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.model.vo.RegistrationActionVO;
import com.campus.activity.model.vo.RegistrationListItemVO;
import com.campus.activity.service.RegistrationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/activities/{activityId}/registrations")
    public Result<RegistrationActionVO> enroll(@PathVariable int activityId) {
        return Result.success(registrationService.enroll(activityId));
    }

    @DeleteMapping("/registrations/{registrationId}")
    public Result<RegistrationActionVO> cancel(@PathVariable int registrationId) {
        return Result.success(registrationService.cancel(registrationId));
    }

    @GetMapping("/registrations/my")
    public Result<PageResult<RegistrationListItemVO>> my(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String status) {
        return Result.success(registrationService.my(page, size, status));
    }

    @GetMapping("/activities/{activityId}/registrations")
    public Result<PageResult<RegistrationListItemVO>> activityRegistrations(@PathVariable int activityId,
                                                                           @RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(required = false) String status) {
        return Result.success(registrationService.activityRegistrations(activityId, page, size, status));
    }

    @PostMapping("/activities/{activityId}/registrations/absences")
    public Result<RegistrationActionVO> markAbsences(@PathVariable int activityId) {
        return Result.success(registrationService.markAbsences(activityId));
    }
}
