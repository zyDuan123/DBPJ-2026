package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.FeedbackRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    private final JdbcTemplate jdbcTemplate;

    public FeedbackService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Map<String, Object> submit(int activityId, FeedbackRequest request) {
        CurrentUser student = Access.require(Role.STUDENT);
        Map<String, Object> registration = findCheckedInRegistration(student.id(), activityId);
        int registrationId = ((Number) registration.get("registration_id")).intValue();

        upsertFeedback(activityId, request, student.id(), registrationId);
        return Map.of(
                "activityId", activityId,
                "registrationId", registrationId,
                "rating", request.rating()
        );
    }

    public Map<String, Object> my(int activityId) {
        CurrentUser student = Access.require(Role.STUDENT);
        var rows = jdbcTemplate.queryForList("""
                SELECT f.feedback_id AS feedbackId, f.rating, f.content,
                       f.created_at AS createdAt, f.updated_at AS updatedAt
                FROM ActivityFeedback f
                WHERE f.student_id = ? AND f.activity_id = ?
                """, student.id(), activityId);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    public Map<String, Object> activityFeedback(int activityId, int page, int size) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateActivityAccess(activityId, user);
        Map<String, Object> summary = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS feedbackCount,
                       COALESCE(ROUND(AVG(rating), 1), 0) AS averageRating,
                       SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) AS positiveCount
                FROM ActivityFeedback
                WHERE activity_id = ?
                """, activityId);
        long total = ((Number) summary.get("feedbackCount")).longValue();
        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT f.feedback_id AS feedbackId, u.username AS studentName, u.student_no AS studentNo,
                       f.rating, f.content, f.created_at AS createdAt, f.updated_at AS updatedAt
                FROM ActivityFeedback f
                JOIN User u ON f.student_id = u.user_id
                WHERE f.activity_id = ?
                ORDER BY f.updated_at DESC
                LIMIT ?, ?
                """, activityId, (page - 1) * size, size);
        return Map.of(
                "summary", normalizeSummary(summary),
                "records", new PageResult<>(list, total, page, size)
        );
    }

    public Map<String, Object> overview() {
        Access.require(Role.ADMIN);
        Map<String, Object> summary = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS feedbackCount,
                       COALESCE(ROUND(AVG(rating), 1), 0) AS averageRating,
                       SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) AS positiveCount
                FROM ActivityFeedback
                """);
        List<Map<String, Object>> topActivities = jdbcTemplate.queryForList("""
                SELECT a.activity_id AS activityId, a.title,
                       COUNT(f.feedback_id) AS feedbackCount,
                       COALESCE(ROUND(AVG(f.rating), 1), 0) AS averageRating
                FROM Activity a
                LEFT JOIN ActivityFeedback f ON a.activity_id = f.activity_id
                GROUP BY a.activity_id, a.title
                HAVING feedbackCount > 0
                ORDER BY averageRating DESC, feedbackCount DESC
                LIMIT 5
                """);
        return Map.of(
                "summary", normalizeSummary(summary),
                "topActivities", topActivities
        );
    }

    private Map<String, Object> findCheckedInRegistration(int studentId, int activityId) {
        var registrations = jdbcTemplate.queryForList("""
                SELECT registration_id, status
                FROM Registration
                WHERE student_id = ? AND activity_id = ?
                """, studentId, activityId);
        if (registrations.isEmpty()) {
            throw new BusinessException(40301, "只有报名学生可以评价活动");
        }
        Map<String, Object> registration = registrations.get(0);
        if (!"CHECKED_IN".equals(registration.get("status"))) {
            throw new BusinessException(40903, "只有已签到活动可以评价");
        }
        return registration;
    }

    private void upsertFeedback(int activityId, FeedbackRequest request, int studentId, int registrationId) {
        var existing = jdbcTemplate.queryForList("""
                SELECT feedback_id
                FROM ActivityFeedback
                WHERE registration_id = ?
                """, registrationId);
        if (existing.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO ActivityFeedback(registration_id, activity_id, student_id, rating, content)
                    VALUES (?, ?, ?, ?, ?)
                    """, registrationId, activityId, studentId, request.rating(), request.content());
            return;
        }
        jdbcTemplate.update("""
                UPDATE ActivityFeedback
                SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP
                WHERE registration_id = ?
                """, request.rating(), request.content(), registrationId);
    }

    private void validateActivityAccess(int activityId, CurrentUser user) {
        if (user.role() == Role.ADMIN) {
            return;
        }
        Integer ownerId = jdbcTemplate.queryForObject("SELECT organizer_id FROM Activity WHERE activity_id = ?", Integer.class, activityId);
        if (ownerId == null || ownerId != user.id()) {
            throw new BusinessException(40301, "只能查看自己活动的反馈");
        }
    }

    private Map<String, Object> normalizeSummary(Map<String, Object> summary) {
        long feedbackCount = ((Number) summary.get("feedbackCount")).longValue();
        BigDecimal averageRating = summary.get("averageRating") instanceof BigDecimal value ? value : BigDecimal.ZERO;
        long positiveCount = summary.get("positiveCount") == null ? 0 : ((Number) summary.get("positiveCount")).longValue();
        int positiveRate = feedbackCount == 0 ? 0 : Math.round((positiveCount * 100f) / feedbackCount);
        return Map.of(
                "feedbackCount", feedbackCount,
                "averageRating", averageRating,
                "positiveRate", positiveRate
        );
    }
}
