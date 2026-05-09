package com.campus.activity.registration;

import com.campus.activity.common.Access;
import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.RegistrationStatus;
import com.campus.activity.common.Result;
import com.campus.activity.common.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RegistrationController {
    private final JdbcTemplate jdbcTemplate;

    public RegistrationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/activities/{activityId}/registrations")
    @Transactional
    public Result<Map<String, Object>> enroll(@PathVariable int activityId) {
        CurrentUser student = Access.require(Role.STUDENT);
        Map<String, Object> activity = lockActivity(activityId);
        if (!"PUBLISHED".equals(activity.get("status"))) {
            throw new BusinessException(40904, "活动当前不可报名");
        }
        LocalDateTime deadline = ((java.sql.Timestamp) activity.get("enroll_deadline")).toLocalDateTime();
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException(40904, "报名已截止");
        }

        var existing = jdbcTemplate.queryForList("""
                SELECT registration_id AS registrationId, status AS registrationStatus, queue_no AS queueNo
                FROM Registration
                WHERE student_id = ? AND activity_id = ?
                """, student.id(), activityId);
        if (!existing.isEmpty()) {
            String status = (String) existing.get(0).get("registrationStatus");
            if ("ENROLLED".equals(status) || "WAITLISTED".equals(status) || "CHECKED_IN".equals(status)) {
                return Result.success(existing.get(0));
            }
        }

        int current = ((Number) activity.get("current_enrollment")).intValue();
        int capacity = ((Number) activity.get("capacity_limit")).intValue();
        if (current < capacity) {
            int registrationId = upsertRegistration(student.id(), activityId, "ENROLLED", null);
            jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment + 1 WHERE activity_id = ?", activityId);
            return Result.success(Map.of(
                    "registrationId", registrationId,
                    "activityId", activityId,
                    "registrationStatus", RegistrationStatus.ENROLLED.name(),
                    "message", "报名成功"
            ));
        }

        Integer queueNo = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(queue_no), 0) + 1
                FROM Registration
                WHERE activity_id = ? AND status = 'WAITLISTED'
                """, Integer.class, activityId);
        int registrationId = upsertRegistration(student.id(), activityId, "WAITLISTED", queueNo);
        return Result.success(Map.of(
                "registrationId", registrationId,
                "activityId", activityId,
                "registrationStatus", RegistrationStatus.WAITLISTED.name(),
                "queueNo", queueNo,
                "message", "名额已满，已进入候补队列"
        ));
    }

    @DeleteMapping("/registrations/{registrationId}")
    @Transactional
    public Result<Map<String, Object>> cancel(@PathVariable int registrationId) {
        CurrentUser user = AuthContext.get();
        Map<String, Object> reg = lockRegistration(registrationId);
        int studentId = ((Number) reg.get("student_id")).intValue();
        int activityId = ((Number) reg.get("activity_id")).intValue();
        String status = (String) reg.get("status");
        if (user.role() != Role.ADMIN && studentId != user.id()) {
            throw new BusinessException(40301, "只能取消自己的报名");
        }
        if ("CANCELLED".equals(status)) {
            return Result.success(Map.of("registrationId", registrationId, "registrationStatus", "CANCELLED"));
        }
        if ("CHECKED_IN".equals(status)) {
            throw new BusinessException(40903, "已签到记录不能取消");
        }

        jdbcTemplate.update("UPDATE Registration SET status = 'CANCELLED' WHERE registration_id = ?", registrationId);
        Integer promotedId = null;
        if ("ENROLLED".equals(status)) {
            lockActivity(activityId);
            jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment - 1 WHERE activity_id = ?", activityId);
            var waiting = jdbcTemplate.queryForList("""
                    SELECT registration_id
                    FROM Registration
                    WHERE activity_id = ? AND status = 'WAITLISTED'
                    ORDER BY queue_no ASC, registration_time ASC
                    LIMIT 1
                    """, activityId);
            if (!waiting.isEmpty()) {
                promotedId = ((Number) waiting.get(0).get("registration_id")).intValue();
                jdbcTemplate.update("UPDATE Registration SET status = 'ENROLLED' WHERE registration_id = ?", promotedId);
                jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment + 1 WHERE activity_id = ?", activityId);
            }
        }

        if (promotedId == null) {
            return Result.success(Map.of("registrationId", registrationId, "registrationStatus", "CANCELLED"));
        }
        return Result.success(Map.of(
                "registrationId", registrationId,
                "registrationStatus", "CANCELLED",
                "promotedRegistrationId", promotedId
        ));
    }

    @GetMapping("/registrations/my")
    public Result<PageResult<Map<String, Object>>> my(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String status) {
        CurrentUser student = Access.require(Role.STUDENT);
        String where = " WHERE r.student_id = ? ";
        Object[] countParams;
        Object[] listParams;
        if (status != null && !status.isBlank()) {
            where += " AND r.status = ? ";
            countParams = new Object[]{student.id(), status};
            listParams = new Object[]{student.id(), status, (page - 1) * size, size};
        } else {
            countParams = new Object[]{student.id()};
            listParams = new Object[]{student.id(), (page - 1) * size, size};
        }
        String from = """
                FROM Registration r
                JOIN Activity a ON r.activity_id = a.activity_id
                JOIN Venue v ON a.venue_id = v.venue_id
                JOIN Campus c ON v.campus_id = c.campus_id
                """;
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + where, Long.class, countParams);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id AS registrationId, r.status AS registrationStatus, r.queue_no AS queueNo,
                       r.registration_time AS registrationTime, r.check_in_time AS checkInTime,
                       a.activity_id AS activityId, a.title, a.start_time AS startTime, a.end_time AS endTime,
                       c.campus_name AS campusName, v.venue_name AS venueName, v.room_number AS roomNumber
                """ + from + where + " ORDER BY a.start_time DESC LIMIT ?, ?", listParams);
        return Result.success(new PageResult<>(rows, total, page, size));
    }

    @GetMapping("/activities/{activityId}/registrations")
    public Result<PageResult<Map<String, Object>>> activityRegistrations(@PathVariable int activityId,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(required = false) String status) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateActivityAccess(activityId, user);
        String where = " WHERE r.activity_id = ? ";
        Object[] countParams;
        Object[] listParams;
        if (status != null && !status.isBlank()) {
            where += " AND r.status = ? ";
            countParams = new Object[]{activityId, status};
            listParams = new Object[]{activityId, status, (page - 1) * size, size};
        } else {
            countParams = new Object[]{activityId};
            listParams = new Object[]{activityId, (page - 1) * size, size};
        }
        String from = " FROM Registration r JOIN User u ON r.student_id = u.user_id ";
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + where, Long.class, countParams);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id AS registrationId, u.username AS studentName, u.student_no AS studentNo,
                       u.phone, r.status AS registrationStatus, r.queue_no AS queueNo,
                       r.registration_time AS registrationTime, r.check_in_time AS checkInTime
                """ + from + where + " ORDER BY r.status, r.queue_no, r.registration_time LIMIT ?, ?", listParams);
        return Result.success(new PageResult<>(rows, total, page, size));
    }

    private int upsertRegistration(int studentId, int activityId, String status, Integer queueNo) {
        var existing = jdbcTemplate.queryForList("""
                SELECT registration_id
                FROM Registration
                WHERE student_id = ? AND activity_id = ?
                """, studentId, activityId);
        if (existing.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO Registration(student_id, activity_id, status, queue_no)
                    VALUES (?, ?, ?, ?)
                    """, studentId, activityId, status, queueNo);
            return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        }
        int id = ((Number) existing.get(0).get("registration_id")).intValue();
        jdbcTemplate.update("""
                UPDATE Registration
                SET status = ?, queue_no = ?, registration_time = CURRENT_TIMESTAMP, check_in_time = NULL
                WHERE registration_id = ?
                """, status, queueNo, id);
        return id;
    }

    private Map<String, Object> lockActivity(int activityId) {
        var rows = jdbcTemplate.queryForList("SELECT * FROM Activity WHERE activity_id = ? FOR UPDATE", activityId);
        if (rows.isEmpty()) {
            throw new BusinessException(40401, "活动不存在");
        }
        return rows.get(0);
    }

    private Map<String, Object> lockRegistration(int registrationId) {
        var rows = jdbcTemplate.queryForList("SELECT * FROM Registration WHERE registration_id = ? FOR UPDATE", registrationId);
        if (rows.isEmpty()) {
            throw new BusinessException(40401, "报名记录不存在");
        }
        return rows.get(0);
    }

    private void validateActivityAccess(int activityId, CurrentUser user) {
        if (user.role() == Role.ADMIN) {
            return;
        }
        Integer ownerId = jdbcTemplate.queryForObject("SELECT organizer_id FROM Activity WHERE activity_id = ?", Integer.class, activityId);
        if (ownerId == null || ownerId != user.id()) {
            throw new BusinessException(40301, "只能查看自己活动的名单");
        }
    }
}
