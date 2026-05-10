package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.Role;
import com.campus.activity.model.mapper.StatsMapper;
import com.campus.activity.model.vo.CampusUsageVO;
import com.campus.activity.model.vo.CategoryPopularityVO;
import com.campus.activity.model.vo.StatsOverviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StatsService {
    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public StatsOverviewVO overview() {
        Access.require(Role.ADMIN);
        return StatsOverviewVO.from(statsMapper.overview());
    }

    public List<CampusUsageVO> campusUsage() {
        Access.require(Role.ADMIN);
        return statsMapper.campusUsage().stream().map(CampusUsageVO::from).toList();
    }

    public List<CategoryPopularityVO> categoryPopularity() {
        Access.require(Role.ADMIN);
        return statsMapper.categoryPopularity().stream().map(CategoryPopularityVO::from).toList();
    }
}
