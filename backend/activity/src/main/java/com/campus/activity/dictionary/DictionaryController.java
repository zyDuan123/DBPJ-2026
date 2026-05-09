package com.campus.activity.dictionary;

import com.campus.activity.common.Access;
import com.campus.activity.common.Result;
import com.campus.activity.common.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    public DictionaryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/campuses")
    public Result<List<Map<String, Object>>> campuses() {
        return Result.success(jdbcTemplate.queryForList("""
                SELECT campus_id AS id, campus_name AS campusName, location
                FROM Campus
                ORDER BY campus_id
                """));
    }

    @PostMapping("/campuses")
    public Result<Map<String, Object>> createCampus(@Valid @RequestBody CampusRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("INSERT INTO Campus(campus_name, location) VALUES (?, ?)", request.campusName(), request.location());
        return Result.success(Map.of("created", true));
    }

    @GetMapping("/venues")
    public Result<List<Map<String, Object>>> venues(@RequestParam(required = false) Integer campusId,
                                                    @RequestParam(required = false, defaultValue = "") String keyword) {
        String like = "%" + keyword + "%";
        if (campusId == null) {
            return Result.success(jdbcTemplate.queryForList("""
                    SELECT v.venue_id AS id, v.venue_name AS venueName, v.room_number AS roomNumber,
                           v.capacity, v.campus_id AS campusId, c.campus_name AS campusName
                    FROM Venue v JOIN Campus c ON v.campus_id = c.campus_id
                    WHERE v.venue_name LIKE ? OR v.room_number LIKE ?
                    ORDER BY v.venue_id
                    """, like, like));
        }
        return Result.success(jdbcTemplate.queryForList("""
                SELECT v.venue_id AS id, v.venue_name AS venueName, v.room_number AS roomNumber,
                       v.capacity, v.campus_id AS campusId, c.campus_name AS campusName
                FROM Venue v JOIN Campus c ON v.campus_id = c.campus_id
                WHERE v.campus_id = ? AND (v.venue_name LIKE ? OR v.room_number LIKE ?)
                ORDER BY v.venue_id
                """, campusId, like, like));
    }

    @PostMapping("/venues")
    public Result<Map<String, Object>> createVenue(@Valid @RequestBody VenueRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("""
                INSERT INTO Venue(venue_name, room_number, capacity, campus_id)
                VALUES (?, ?, ?, ?)
                """, request.venueName(), request.roomNumber(), request.capacity(), request.campusId());
        return Result.success(Map.of("created", true));
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(jdbcTemplate.queryForList("""
                SELECT category_id AS id, category_name AS categoryName
                FROM Category
                ORDER BY category_id
                """));
    }

    @PostMapping("/categories")
    public Result<Map<String, Object>> createCategory(@Valid @RequestBody CategoryRequest request) {
        Access.require(Role.ADMIN);
        jdbcTemplate.update("INSERT INTO Category(category_name) VALUES (?)", request.categoryName());
        return Result.success(Map.of("created", true));
    }

    public record CampusRequest(@NotBlank String campusName, String location) {
    }

    public record VenueRequest(@NotBlank String venueName, @NotBlank String roomNumber, @Min(1) int capacity, int campusId) {
    }

    public record CategoryRequest(@NotBlank String categoryName) {
    }
}
