package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {
    private final JdbcTemplate jdbcTemplate;

    public StatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> overview() {
        Access.require(Role.ADMIN);
        return Map.of(
                "activityCount", count("SELECT COUNT(*) FROM Activity"),
                "pendingReviewCount", count("SELECT COUNT(*) FROM Activity WHERE status = 'PENDING_REVIEW'"),
                "publishedCount", count("SELECT COUNT(*) FROM Activity WHERE status = 'PUBLISHED'"),
                "registrationCount", count("SELECT COUNT(*) FROM Registration WHERE status IN ('ENROLLED', 'WAITLISTED', 'CHECKED_IN')"),
                "checkedInCount", count("SELECT COUNT(*) FROM Registration WHERE status = 'CHECKED_IN'")
        );
    }

    public List<Map<String, Object>> campusUsage() {
        Access.require(Role.ADMIN);
        return jdbcTemplate.queryForList("""
                SELECT c.campus_id AS campusId, c.campus_name AS campusName,
                       COUNT(DISTINCT a.activity_id) AS activityCount,
                       COUNT(DISTINCT v.venue_id) AS venueCount
                FROM Campus c
                LEFT JOIN Venue v ON c.campus_id = v.campus_id
                LEFT JOIN Activity a ON v.venue_id = a.venue_id
                GROUP BY c.campus_id, c.campus_name
                ORDER BY c.campus_id
                """);
    }

    public List<Map<String, Object>> categoryPopularity() {
        Access.require(Role.ADMIN);
        return jdbcTemplate.queryForList("""
                SELECT cat.category_id AS categoryId, cat.category_name AS categoryName,
                       COUNT(a.activity_id) AS activityCount,
                       COALESCE(AVG(a.current_enrollment), 0) AS averageEnrollment
                FROM Category cat
                LEFT JOIN Activity a ON cat.category_id = a.category_id
                GROUP BY cat.category_id, cat.category_name
                ORDER BY activityCount DESC
                """);
    }

    private Long count(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
