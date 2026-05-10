package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.Role;
import com.campus.activity.model.mapper.StatsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {
    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public Map<String, Object> overview() {
        Access.require(Role.ADMIN);
        return statsMapper.overview();
    }

    public List<Map<String, Object>> campusUsage() {
        Access.require(Role.ADMIN);
        return statsMapper.campusUsage();
    }

    public List<Map<String, Object>> categoryPopularity() {
        Access.require(Role.ADMIN);
        return statsMapper.categoryPopularity();
    }
}
