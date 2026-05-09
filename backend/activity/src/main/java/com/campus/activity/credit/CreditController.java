package com.campus.activity.credit;

import com.campus.activity.common.Access;
import com.campus.activity.common.AuthContext;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.common.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {
    private final JdbcTemplate jdbcTemplate;

    public CreditController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> my(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        CurrentUser student = Access.require(Role.STUDENT);
        return Result.success(studentCredit(student.id(), page, size));
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Access.require(Role.ADMIN);
        Map<String, Object> summary = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS recordCount,
                       COALESCE(SUM(change_value), 0) AS totalChange,
                       SUM(CASE WHEN reason_type = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount,
                       SUM(CASE WHEN reason_type = 'CHECK_IN' THEN 1 ELSE 0 END) AS checkInCreditCount
                FROM CreditRecord
                """);
        List<Map<String, Object>> riskStudents = jdbcTemplate.queryForList("""
                SELECT u.user_id AS studentId, u.username AS studentName, u.student_no AS studentNo,
                       100 + COALESCE(SUM(c.change_value), 0) AS creditScore,
                       SUM(CASE WHEN c.reason_type = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
                FROM User u
                LEFT JOIN CreditRecord c ON u.user_id = c.student_id
                WHERE u.role = 'STUDENT'
                GROUP BY u.user_id, u.username, u.student_no
                HAVING creditScore < 100 OR absentCount > 0
                ORDER BY creditScore ASC, absentCount DESC
                LIMIT 10
                """);
        return Result.success(Map.of(
                "summary", summary,
                "riskStudents", riskStudents
        ));
    }

    private Map<String, Object> studentCredit(int studentId, int page, int size) {
        Long totalChange = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(change_value), 0)
                FROM CreditRecord
                WHERE student_id = ?
                """, Long.class, studentId);
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CreditRecord WHERE student_id = ?", Long.class, studentId);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                SELECT c.record_id AS recordId, c.change_value AS changeValue, c.reason_type AS reasonType,
                       c.reason, c.created_at AS createdAt,
                       a.activity_id AS activityId, a.title AS activityTitle
                FROM CreditRecord c
                LEFT JOIN Activity a ON c.activity_id = a.activity_id
                WHERE c.student_id = ?
                ORDER BY c.created_at DESC, c.record_id DESC
                LIMIT ?, ?
                """, studentId, (page - 1) * size, size);
        return Map.of(
                "score", 100 + (totalChange == null ? 0 : totalChange),
                "records", new PageResult<>(records, total, page, size)
        );
    }
}
