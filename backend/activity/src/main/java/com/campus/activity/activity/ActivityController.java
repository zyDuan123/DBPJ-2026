package com.campus.activity.activity;

import com.campus.activity.common.Access;
import com.campus.activity.common.ActivityStatus;
import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.common.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {
    private final JdbcTemplate jdbcTemplate;

    public ActivityController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer campusId,
                                                        @RequestParam(required = false) Integer categoryId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "false") boolean mine) {
        CurrentUser user = AuthContext.get();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (user.role() == Role.STUDENT) {
            where.append(" AND a.status IN ('PUBLISHED', 'ONGOING', 'FINISHED') ");
        }
        if (mine) {
            where.append(" AND a.organizer_id = ? ");
            params.add(user.id());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND a.title LIKE ? ");
            params.add("%" + keyword + "%");
        }
        if (campusId != null) {
            where.append(" AND c.campus_id = ? ");
            params.add(campusId);
        }
        if (categoryId != null) {
            where.append(" AND a.category_id = ? ");
            params.add(categoryId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND a.status = ? ");
            params.add(status);
        }

        String from = """
                FROM Activity a
                JOIN Venue v ON a.venue_id = v.venue_id
                JOIN Campus c ON v.campus_id = c.campus_id
                JOIN Category cat ON a.category_id = cat.category_id
                JOIN User u ON a.organizer_id = u.user_id
                """;
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + where, Long.class, params.toArray());
        params.add((page - 1) * size);
        params.add(size);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT a.activity_id AS id, a.title, a.poster_url AS posterUrl,
                       a.start_time AS startTime, a.end_time AS endTime, a.enroll_deadline AS enrollDeadline,
                       c.campus_name AS campusName, v.venue_name AS venueName, v.room_number AS roomNumber,
                       cat.category_name AS categoryName, a.venue_id AS venueId, a.category_id AS categoryId,
                       a.capacity_limit AS capacityLimit,
                       a.current_enrollment AS currentEnrollment, a.status, a.reject_reason AS rejectReason,
                       u.username AS organizerName
                """ + from + where + " ORDER BY a.start_time DESC LIMIT ?, ?", params.toArray());
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @GetMapping("/{activityId}")
    public Result<Map<String, Object>> detail(@PathVariable int activityId) {
        var rows = jdbcTemplate.queryForList("""
                SELECT a.activity_id AS id, a.title, a.poster_url AS posterUrl, a.description,
                       a.start_time AS startTime, a.end_time AS endTime, a.enroll_deadline AS enrollDeadline,
                       c.campus_name AS campusName, v.venue_name AS venueName, v.room_number AS roomNumber,
                       cat.category_name AS categoryName, a.venue_id AS venueId, a.category_id AS categoryId,
                       a.capacity_limit AS capacityLimit,
                       a.current_enrollment AS currentEnrollment, a.status, a.reject_reason AS rejectReason,
                       u.username AS organizerName, a.organizer_id AS organizerId
                FROM Activity a
                JOIN Venue v ON a.venue_id = v.venue_id
                JOIN Campus c ON v.campus_id = c.campus_id
                JOIN Category cat ON a.category_id = cat.category_id
                JOIN User u ON a.organizer_id = u.user_id
                WHERE a.activity_id = ?
                """, activityId);
        if (rows.isEmpty()) {
            throw new BusinessException(40401, "活动不存在");
        }
        Map<String, Object> detail = rows.get(0);
        CurrentUser user = AuthContext.get();
        if (user.role() == Role.STUDENT) {
            var regs = jdbcTemplate.queryForList("""
                    SELECT registration_id AS registrationId, status AS registrationStatus
                    FROM Registration
                    WHERE student_id = ? AND activity_id = ?
                    """, user.id(), activityId);
            if (!regs.isEmpty()) {
                detail.putAll(regs.get(0));
            }
        }
        return Result.success(detail);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody ActivityRequest request) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateTime(request);
        jdbcTemplate.update("""
                INSERT INTO Activity(title, venue_id, category_id, start_time, end_time, enroll_deadline,
                                     capacity_limit, poster_url, description, status, organizer_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT', ?)
                """, request.title(), request.venueId(), request.categoryId(), request.startTime(), request.endTime(),
                request.enrollDeadline(), request.capacityLimit(), request.posterUrl(), request.description(), user.id());
        Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return Result.success(Map.of("id", id, "status", ActivityStatus.DRAFT.name()));
    }

    @PutMapping("/{activityId}")
    public Result<Map<String, Object>> update(@PathVariable int activityId, @Valid @RequestBody ActivityRequest request) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateOwnerOrAdmin(activityId, user);
        String status = jdbcTemplate.queryForObject("SELECT status FROM Activity WHERE activity_id = ?", String.class, activityId);
        if (user.role() != Role.ADMIN && !List.of("DRAFT", "REJECTED").contains(status)) {
            throw new BusinessException(40903, "当前状态不允许修改活动");
        }
        validateTime(request);
        jdbcTemplate.update("""
                UPDATE Activity
                SET title = ?, venue_id = ?, category_id = ?, start_time = ?, end_time = ?, enroll_deadline = ?,
                    capacity_limit = ?, poster_url = ?, description = ?
                WHERE activity_id = ?
                """, request.title(), request.venueId(), request.categoryId(), request.startTime(), request.endTime(),
                request.enrollDeadline(), request.capacityLimit(), request.posterUrl(), request.description(), activityId);
        return Result.success(Map.of("updated", true));
    }

    @PostMapping("/{activityId}/submit")
    @Transactional
    public Result<Map<String, Object>> submit(@PathVariable int activityId) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateOwnerOrAdmin(activityId, user);
        String status = jdbcTemplate.queryForObject("SELECT status FROM Activity WHERE activity_id = ?", String.class, activityId);
        if (!List.of("DRAFT", "REJECTED").contains(status)) {
            throw new BusinessException(40903, "只有草稿或驳回活动可以提交审核");
        }
        validateVenueConflict(activityId);
        jdbcTemplate.update("UPDATE Activity SET status = 'PENDING_REVIEW', reject_reason = NULL WHERE activity_id = ?", activityId);
        return Result.success(Map.of("status", ActivityStatus.PENDING_REVIEW.name()));
    }

    @PostMapping("/{activityId}/review")
    @Transactional
    public Result<Map<String, Object>> review(@PathVariable int activityId, @Valid @RequestBody ReviewRequest request) {
        CurrentUser admin = Access.require(Role.ADMIN);
        String status = jdbcTemplate.queryForObject("SELECT status FROM Activity WHERE activity_id = ?", String.class, activityId);
        if (!"PENDING_REVIEW".equals(status)) {
            throw new BusinessException(40903, "只有待审核活动可以审核");
        }
        if ("APPROVED".equals(request.result())) {
            validateVenueConflict(activityId);
            jdbcTemplate.update("UPDATE Activity SET status = 'PUBLISHED', admin_id = ?, reject_reason = NULL WHERE activity_id = ?", admin.id(), activityId);
            return Result.success(Map.of("status", ActivityStatus.PUBLISHED.name()));
        }
        if ("REJECTED".equals(request.result())) {
            if (request.reason() == null || request.reason().isBlank()) {
                throw new BusinessException(40001, "驳回原因不能为空");
            }
            jdbcTemplate.update("UPDATE Activity SET status = 'REJECTED', admin_id = ?, reject_reason = ? WHERE activity_id = ?",
                    admin.id(), request.reason(), activityId);
            return Result.success(Map.of("status", ActivityStatus.REJECTED.name()));
        }
        throw new BusinessException(40001, "审核结果不合法");
    }

    @PostMapping("/{activityId}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable int activityId) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateOwnerOrAdmin(activityId, user);
        jdbcTemplate.update("UPDATE Activity SET status = 'CANCELLED' WHERE activity_id = ?", activityId);
        return Result.success(Map.of("status", ActivityStatus.CANCELLED.name()));
    }

    private void validateOwnerOrAdmin(int activityId, CurrentUser user) {
        Integer ownerId = jdbcTemplate.queryForObject("SELECT organizer_id FROM Activity WHERE activity_id = ?", Integer.class, activityId);
        if (user.role() != Role.ADMIN && ownerId != user.id()) {
            throw new BusinessException(40301, "只能管理自己创建的活动");
        }
    }

    private void validateTime(ActivityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(40001, "结束时间必须晚于开始时间");
        }
        if (request.enrollDeadline().isAfter(request.startTime())) {
            throw new BusinessException(40001, "报名截止时间不得晚于活动开始时间");
        }
    }

    private void validateVenueConflict(int activityId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM Activity target
                JOIN Activity other ON target.venue_id = other.venue_id
                WHERE target.activity_id = ?
                  AND other.activity_id <> target.activity_id
                  AND other.status IN ('PENDING_REVIEW', 'PUBLISHED', 'ONGOING')
                  AND target.start_time < other.end_time
                  AND target.end_time > other.start_time
                """, Integer.class, activityId);
        if (count != null && count > 0) {
            throw new BusinessException(40902, "该场地在此时段已被占用");
        }
    }

    public record ActivityRequest(@NotBlank String title,
                                  int venueId,
                                  int categoryId,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime,
                                  LocalDateTime enrollDeadline,
                                  @Min(1) int capacityLimit,
                                  String posterUrl,
                                  String description) {
    }

    public record ReviewRequest(@NotBlank String result, String reason) {
    }
}
