package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.model.dto.CampusRequest;
import com.campus.activity.model.dto.CategoryRequest;
import com.campus.activity.model.dto.VenueRequest;
import com.campus.activity.model.vo.CampusVO;
import com.campus.activity.model.vo.CategoryVO;
import com.campus.activity.model.vo.MutationResultVO;
import com.campus.activity.model.vo.VenueVO;
import com.campus.activity.service.DictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DictionaryController {
    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/campuses")
    public Result<List<CampusVO>> campuses() {
        return Result.success(dictionaryService.campuses());
    }

    @PostMapping("/campuses")
    public Result<MutationResultVO> createCampus(@Valid @RequestBody CampusRequest request) {
        return Result.success(dictionaryService.createCampus(request));
    }

    @GetMapping("/venues")
    public Result<List<VenueVO>> venues(@RequestParam(required = false) Integer campusId,
                                        @RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(dictionaryService.venues(campusId, keyword));
    }

    @PostMapping("/venues")
    public Result<MutationResultVO> createVenue(@Valid @RequestBody VenueRequest request) {
        return Result.success(dictionaryService.createVenue(request));
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(dictionaryService.categories());
    }

    @PostMapping("/categories")
    public Result<MutationResultVO> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.success(dictionaryService.createCategory(request));
    }
}
