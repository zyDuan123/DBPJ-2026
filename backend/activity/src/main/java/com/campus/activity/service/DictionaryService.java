package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.CampusRequest;
import com.campus.activity.model.dto.CategoryRequest;
import com.campus.activity.model.dto.VenueRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DictionaryService {
    private final JdbcTemplate jdbcTemplate;

    public DictionaryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> campuses() {
        return jdbcTemplate.queryForList("""
                SELECT campus_id AS id, campus_name AS campusName, location
                FROM Campus
                ORDER BY campus_id
                """);
    }

    @Transactional
    public Map<String, Object> createCampus(CampusRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("INSERT INTO Campus(campus_name, location) VALUES (?, ?)", request.campusName(), request.location());
        return Map.of("created", true);
    }

    public List<Map<String, Object>> venues(Integer campusId, String keyword) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        if (campusId == null) {
            return jdbcTemplate.queryForList("""
                    SELECT v.venue_id AS id, v.venue_name AS venueName, v.room_number AS roomNumber,
                           v.capacity, v.campus_id AS campusId, c.campus_name AS campusName
                    FROM Venue v JOIN Campus c ON v.campus_id = c.campus_id
                    WHERE v.venue_name LIKE ? OR v.room_number LIKE ?
                    ORDER BY v.venue_id
                    """, like, like);
        }
        return jdbcTemplate.queryForList("""
                SELECT v.venue_id AS id, v.venue_name AS venueName, v.room_number AS roomNumber,
                       v.capacity, v.campus_id AS campusId, c.campus_name AS campusName
                FROM Venue v JOIN Campus c ON v.campus_id = c.campus_id
                WHERE v.campus_id = ? AND (v.venue_name LIKE ? OR v.room_number LIKE ?)
                ORDER BY v.venue_id
                """, campusId, like, like);
    }

    @Transactional
    public Map<String, Object> createVenue(VenueRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("""
                INSERT INTO Venue(venue_name, room_number, capacity, campus_id)
                VALUES (?, ?, ?, ?)
                """, request.venueName(), request.roomNumber(), request.capacity(), request.campusId());
        return Map.of("created", true);
    }

    public List<Map<String, Object>> categories() {
        return jdbcTemplate.queryForList("""
                SELECT category_id AS id, category_name AS categoryName
                FROM Category
                ORDER BY category_id
                """);
    }

    @Transactional
    public Map<String, Object> createCategory(CategoryRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("INSERT INTO Category(category_name) VALUES (?)", request.categoryName());
        return Map.of("created", true);
    }
}
