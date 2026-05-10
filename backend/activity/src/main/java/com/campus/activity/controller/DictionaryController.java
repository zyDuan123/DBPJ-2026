package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.model.dto.CampusRequest;
import com.campus.activity.model.dto.CategoryRequest;
import com.campus.activity.model.dto.VenueRequest;
import com.campus.activity.service.DictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DictionaryController {
    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/campuses")
    public Result<List<Map<String, Object>>> campuses() {
        return Result.success(dictionaryService.campuses());
    }

    @PostMapping("/campuses")
    public Result<Map<String, Object>> createCampus(@Valid @RequestBody CampusRequest request) {
        return Result.success(dictionaryService.createCampus(request));
    }

    @GetMapping("/venues")
    public Result<List<Map<String, Object>>> venues(@RequestParam(required = false) Integer campusId,
                                                    @RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(dictionaryService.venues(campusId, keyword));
    }

    @PostMapping("/venues")
    public Result<Map<String, Object>> createVenue(@Valid @RequestBody VenueRequest request) {
        return Result.success(dictionaryService.createVenue(request));
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(dictionaryService.categories());
    }

    @PostMapping("/categories")
    public Result<Map<String, Object>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.success(dictionaryService.createCategory(request));
    }
}
