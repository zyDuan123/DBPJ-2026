package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.RegistrationStatus;
import com.campus.activity.common.Role;
import com.campus.activity.common.TimeValues;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RegistrationService {
    private final JdbcTemplate jdbcTemplate;

    public RegistrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Map<String, Object> enroll(int activityId) {
        CurrentUser student = Access.require(Role.STUDENT);
        Map<String, Object> activity = lockActivity(activityId);
        validateActivityOpenForEnrollment(activity);

        Map<String, Object> existing = findReusableRegistration(student.id(), activityId);
        if (!existing.isEmpty()) {
            return existing;
        }

        int current = ((Number) activity.get("current_enrollment")).intValue();
        int capacity = ((Number) activity.get("capacity_limit")).intValue();
        if (current < capacity) {
            return enrollDirectly(student.id(), activityId);
        }
        return waitlist(student.id(), activityId);
    }

    @Transactional
    public Map<String, Object> cancel(int registrationId) {
        CurrentUser user = AuthContext.get();
        Map<String, Object> registration = lockRegistration(registrationId);
        int studentId = ((Number) registration.get("student_id")).intValue();
        int activityId = ((Number) registration.get("activity_id")).intValue();
        String status = (String) registration.get("status");
        validateCancellationAllowed(user, studentId, status);

        if ("CANCELLED".equals(status)) {
            return Map.of("registrationId", registrationId, "registrationStatus", "CANCELLED");
        }

        jdbcTemplate.update("UPDATE Registration SET status = 'CANCELLED' WHERE registration_id = ?", registrationId);
        Integer promotedId = "ENROLLED".equals(status) ? promoteNextWaitlisted(activityId) : null;
        if (promotedId == null) {
            return Map.of("registrationId", registrationId, "registrationStatus", "CANCELLED");
        }
        return Map.of(
                "registrationId", registrationId,
                "registrationStatus", "CANCELLED",
                "promotedRegistrationId", promotedId
        );
    }

    public PageResult<Map<String, Object>> my(int page, int size, String status) {
        CurrentUser student = Access.require(Role.STUDENT);
        QueryParts query = studentRegistrationQuery(student.id(), status, page, size);
        String from = """
                FROM Registration r
                JOIN Activity a ON r.activity_id = a.activity_id
                JOIN Venue v ON a.venue_id = v.venue_id
                JOIN Campus c ON v.campus_id = c.campus_id
                """;
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + query.where(), Long.class, query.countParams());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id AS registrationId, r.status AS registrationStatus, r.queue_no AS queueNo,
                       r.registration_time AS registrationTime, r.check_in_time AS checkInTime,
                       a.activity_id AS activityId, a.title, a.start_time AS startTime, a.end_time AS endTime,
                       c.campus_name AS campusName, v.venue_name AS venueName, v.room_number AS roomNumber
                """ + from + query.where() + " ORDER BY a.start_time DESC LIMIT ?, ?", query.listParams());
        return new PageResult<>(rows, total, page, size);
    }

    public PageResult<Map<String, Object>> activityRegistrations(int activityId, int page, int size, String status) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateActivityAccess(activityId, user);
        QueryParts query = activityRegistrationQuery(activityId, status, page, size);
        String from = " FROM Registration r JOIN User u ON r.student_id = u.user_id ";
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + query.where(), Long.class, query.countParams());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id AS registrationId, u.username AS studentName, u.student_no AS studentNo,
                       u.phone, r.status AS registrationStatus, r.queue_no AS queueNo,
                       r.registration_time AS registrationTime, r.check_in_time AS checkInTime
                """ + from + query.where() + " ORDER BY r.status, r.queue_no, r.registration_time LIMIT ?, ?", query.listParams());
        return new PageResult<>(rows, total, page, size);
    }

    @Transactional
    public Map<String, Object> markAbsences(int activityId) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateActivityAccess(activityId, user);
        Map<String, Object> activity = lockActivity(activityId);
        LocalDateTime endTime = TimeValues.toLocalDateTime(activity.get("end_time"));
        if (endTime != null && LocalDateTime.now().isBefore(endTime)) {
            throw new BusinessException(40903, "活动结束后才能标记缺勤");
        }
        int absentCount = jdbcTemplate.update("""
                UPDATE Registration
                SET status = 'ABSENT'
                WHERE activity_id = ? AND status = 'ENROLLED'
                """, activityId);
        if (absentCount > 0) {
            recordAbsences(activityId, absentCount, user.id());
        }
        return Map.of(
                "activityId", activityId,
                "absentCount", absentCount
        );
    }

    private void validateActivityOpenForEnrollment(Map<String, Object> activity) {
        if (!"PUBLISHED".equals(activity.get("status"))) {
            throw new BusinessException(40904, "活动当前不可报名");
        }
        LocalDateTime deadline = TimeValues.toLocalDateTime(activity.get("enroll_deadline"));
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException(40904, "报名已截止");
        }
    }

    private Map<String, Object> findReusableRegistration(int studentId, int activityId) {
        var existing = jdbcTemplate.queryForList("""
                SELECT registration_id AS registrationId, status AS registrationStatus, queue_no AS queueNo
                FROM Registration
                WHERE student_id = ? AND activity_id = ?
                """, studentId, activityId);
        if (existing.isEmpty()) {
            return Map.of();
        }
        String status = (String) existing.get(0).get("registrationStatus");
        if ("ENROLLED".equals(status) || "WAITLISTED".equals(status) || "CHECKED_IN".equals(status)) {
            return existing.get(0);
        }
        return Map.of();
    }

    private Map<String, Object> enrollDirectly(int studentId, int activityId) {
        int registrationId = upsertRegistration(studentId, activityId, "ENROLLED", null);
        jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment + 1 WHERE activity_id = ?", activityId);
        return Map.of(
                "registrationId", registrationId,
                "activityId", activityId,
                "registrationStatus", RegistrationStatus.ENROLLED.name(),
                "message", "报名成功"
        );
    }

    private Map<String, Object> waitlist(int studentId, int activityId) {
        Integer queueNo = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(queue_no), 0) + 1
                FROM Registration
                WHERE activity_id = ? AND status = 'WAITLISTED'
                """, Integer.class, activityId);
        int registrationId = upsertRegistration(studentId, activityId, "WAITLISTED", queueNo);
        return Map.of(
                "registrationId", registrationId,
                "activityId", activityId,
                "registrationStatus", RegistrationStatus.WAITLISTED.name(),
                "queueNo", queueNo,
                "message", "名额已满，已进入候补队列"
        );
    }

    private void validateCancellationAllowed(CurrentUser user, int studentId, String status) {
        if (user.role() != Role.ADMIN && studentId != user.id()) {
            throw new BusinessException(40301, "只能取消自己的报名");
        }
        if ("CHECKED_IN".equals(status)) {
            throw new BusinessException(40903, "已签到记录不能取消");
        }
    }

    private Integer promoteNextWaitlisted(int activityId) {
        lockActivity(activityId);
        jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment - 1 WHERE activity_id = ?", activityId);
        var waiting = jdbcTemplate.queryForList("""
                SELECT registration_id
                FROM Registration
                WHERE activity_id = ? AND status = 'WAITLISTED'
                ORDER BY queue_no ASC, registration_time ASC
                LIMIT 1
                """, activityId);
        if (waiting.isEmpty()) {
            return null;
        }
        Integer promotedId = ((Number) waiting.get(0).get("registration_id")).intValue();
        jdbcTemplate.update("UPDATE Registration SET status = 'ENROLLED' WHERE registration_id = ?", promotedId);
        jdbcTemplate.update("UPDATE Activity SET current_enrollment = current_enrollment + 1 WHERE activity_id = ?", activityId);
        return promotedId;
    }

    private void recordAbsences(int activityId, int absentCount, int operatorId) {
        jdbcTemplate.update("""
                UPDATE Activity
                SET current_enrollment = GREATEST(current_enrollment - ?, 0)
                WHERE activity_id = ?
                """, absentCount, activityId);
        jdbcTemplate.update("""
                INSERT IGNORE INTO CreditRecord(student_id, activity_id, registration_id, change_value, reason_type, reason, operator_id)
                SELECT student_id, activity_id, registration_id, -10, 'ABSENT', '活动结束后未完成签到', ?
                FROM Registration
                WHERE activity_id = ? AND status = 'ABSENT'
                """, operatorId, activityId);
    }

    private QueryParts studentRegistrationQuery(int studentId, String status, int page, int size) {
        String where = " WHERE r.student_id = ? ";
        if (status != null && !status.isBlank()) {
            return new QueryParts(where + " AND r.status = ? ",
                    new Object[]{studentId, status},
                    new Object[]{studentId, status, (page - 1) * size, size});
        }
        return new QueryParts(where,
                new Object[]{studentId},
                new Object[]{studentId, (page - 1) * size, size});
    }

    private QueryParts activityRegistrationQuery(int activityId, String status, int page, int size) {
        String where = " WHERE r.activity_id = ? ";
        if (status != null && !status.isBlank()) {
            return new QueryParts(where + " AND r.status = ? ",
                    new Object[]{activityId, status},
                    new Object[]{activityId, status, (page - 1) * size, size});
        }
        return new QueryParts(where,
                new Object[]{activityId},
                new Object[]{activityId, (page - 1) * size, size});
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

    private record QueryParts(String where, Object[] countParams, Object[] listParams) {
    }
}
