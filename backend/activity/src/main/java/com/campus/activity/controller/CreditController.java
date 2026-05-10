package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.model.vo.CreditMyVO;
import com.campus.activity.model.vo.CreditOverviewVO;
import com.campus.activity.service.CreditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {
    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/my")
    public Result<CreditMyVO> my(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        return Result.success(creditService.my(page, size));
    }

    @GetMapping("/overview")
    public Result<CreditOverviewVO> overview() {
        return Result.success(creditService.overview());
    }
}
