package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.CampusRequest;
import com.campus.activity.model.dto.CategoryRequest;
import com.campus.activity.model.dto.VenueRequest;
import com.campus.activity.model.mapper.CampusMapper;
import com.campus.activity.model.mapper.CategoryMapper;
import com.campus.activity.model.mapper.VenueMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DictionaryService {
    private final CampusMapper campusMapper;
    private final VenueMapper venueMapper;
    private final CategoryMapper categoryMapper;

    public DictionaryService(CampusMapper campusMapper, VenueMapper venueMapper, CategoryMapper categoryMapper) {
        this.campusMapper = campusMapper;
        this.venueMapper = venueMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<Map<String, Object>> campuses() {
        return campusMapper.listCampuses();
    }

    @Transactional
    public Map<String, Object> createCampus(CampusRequest request) {
        Access.require(Role.ADMIN);
        campusMapper.createCampus(request.campusName(), request.location());
        return Map.of("created", true);
    }

    public List<Map<String, Object>> venues(Integer campusId, String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return venueMapper.listVenues(campusId, like);
    }

    @Transactional
    public Map<String, Object> createVenue(VenueRequest request) {
        Access.require(Role.ADMIN);
        venueMapper.createVenue(request.venueName(), request.roomNumber(), request.capacity(), request.campusId());
        return Map.of("created", true);
    }

    public List<Map<String, Object>> categories() {
        return categoryMapper.listCategories();
    }

    @Transactional
    public Map<String, Object> createCategory(CategoryRequest request) {
        Access.require(Role.ADMIN);
        categoryMapper.createCategory(request.categoryName());
        return Map.of("created", true);
    }
}
