package com.campus.activity.feedback;

import com.campus.activity.common.Access;
import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.common.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    private final JdbcTemplate jdbcTemplate;

    public FeedbackController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/activities/{activityId}/feedback")
    @Transactional
    public Result<Map<String, Object>> submit(@PathVariable int activityId, @Valid @RequestBody FeedbackRequest request) {
        CurrentUser student = Access.require(Role.STUDENT);
        var registrations = jdbcTemplate.queryForList("""
                SELECT registration_id, status
                FROM Registration
                WHERE student_id = ? AND activity_id = ?
                """, student.id(), activityId);
        if (registrations.isEmpty()) {
            throw new BusinessException(40301, "只有报名学生可以评价活动");
        }
        Map<String, Object> registration = registrations.get(0);
        if (!"CHECKED_IN".equals(registration.get("status"))) {
            throw new BusinessException(40903, "只有已签到活动可以评价");
        }

        int registrationId = ((Number) registration.get("registration_id")).intValue();
        var existing = jdbcTemplate.queryForList("""
                SELECT feedback_id
                FROM ActivityFeedback
                WHERE registration_id = ?
                """, registrationId);
        if (existing.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO ActivityFeedback(registration_id, activity_id, student_id, rating, content)
                    VALUES (?, ?, ?, ?, ?)
                    """, registrationId, activityId, student.id(), request.rating(), request.content());
        } else {
            jdbcTemplate.update("""
                    UPDATE ActivityFeedback
                    SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE registration_id = ?
                    """, request.rating(), request.content(), registrationId);
        }

        return Result.success(Map.of(
                "activityId", activityId,
                "registrationId", registrationId,
                "rating", request.rating()
        ));
    }

    @GetMapping("/activities/{activityId}/feedback/my")
    public Result<Map<String, Object>> my(@PathVariable int activityId) {
        CurrentUser student = Access.require(Role.STUDENT);
        var rows = jdbcTemplate.queryForList("""
                SELECT f.feedback_id AS feedbackId, f.rating, f.content,
                       f.created_at AS createdAt, f.updated_at AS updatedAt
                FROM ActivityFeedback f
                WHERE f.student_id = ? AND f.activity_id = ?
                """, student.id(), activityId);
        return Result.success(rows.isEmpty() ? Map.of() : rows.get(0));
    }

    @GetMapping("/activities/{activityId}/feedback")
    public Result<Map<String, Object>> activityFeedback(@PathVariable int activityId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
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
        return Result.success(Map.of(
                "summary", normalizeSummary(summary),
                "records", new PageResult<>(list, total, page, size)
        ));
    }

    @GetMapping("/feedback/overview")
    public Result<Map<String, Object>> overview() {
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
        return Result.success(Map.of(
                "summary", normalizeSummary(summary),
                "topActivities", topActivities
        ));
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

    public record FeedbackRequest(@Min(1) @Max(5) int rating, String content) {
    }
}
