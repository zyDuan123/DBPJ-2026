package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.CheckInRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CheckInService {
    private static final int CHECK_IN_CODE_TTL_SECONDS = 2 * 3600;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JdbcTemplate jdbcTemplate;
    private final String secret;

    public CheckInService(JdbcTemplate jdbcTemplate, @Value("${app.auth-secret}") String secret) {
        this.jdbcTemplate = jdbcTemplate;
        this.secret = secret;
    }

    public Map<String, Object> code(int registrationId) {
        CurrentUser student = Access.require(Role.STUDENT);
        var rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id, r.student_id, r.status, a.end_time
                FROM Registration r JOIN Activity a ON r.activity_id = a.activity_id
                WHERE r.registration_id = ?
                """, registrationId);
        if (rows.isEmpty()) {
            throw new BusinessException(40401, "报名记录不存在");
        }
        Map<String, Object> row = rows.get(0);
        validateCanGenerateCode(row, student);

        long expiresAt = Instant.now().plusSeconds(CHECK_IN_CODE_TTL_SECONDS).getEpochSecond();
        String payload = registrationId + ":" + expiresAt;
        String token = base64(payload) + "." + sign(payload);
        return Map.of(
                "registrationId", registrationId,
                "checkInCode", token,
                "expiresAt", LocalDateTime.ofInstant(Instant.ofEpochSecond(expiresAt), ZoneId.systemDefault()).toString()
        );
    }

    @Transactional
    public Map<String, Object> checkIn(CheckInRequest request) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        int registrationId = parseCode(request.checkInCode());
        var rows = jdbcTemplate.queryForList("""
                SELECT r.registration_id, r.status, a.organizer_id
                FROM Registration r JOIN Activity a ON r.activity_id = a.activity_id
                WHERE r.registration_id = ?
                FOR UPDATE
                """, registrationId);
        if (rows.isEmpty()) {
            throw new BusinessException(40401, "报名记录不存在");
        }
        Map<String, Object> row = rows.get(0);
        validateCanCheckIn(row, user);

        String status = (String) row.get("status");
        if ("CHECKED_IN".equals(status)) {
            return Map.of("registrationId", registrationId, "registrationStatus", "CHECKED_IN");
        }
        if (!"ENROLLED".equals(status)) {
            throw new BusinessException(40903, "只有正选报名可以签到");
        }
        jdbcTemplate.update("""
                UPDATE Registration
                SET status = 'CHECKED_IN', check_in_time = CURRENT_TIMESTAMP
                WHERE registration_id = ?
                """, registrationId);
        jdbcTemplate.update("""
                INSERT IGNORE INTO CreditRecord(student_id, activity_id, registration_id, change_value, reason_type, reason, operator_id)
                SELECT r.student_id, r.activity_id, r.registration_id, 1, 'CHECK_IN', '按时完成活动签到', ?
                FROM Registration r
                WHERE r.registration_id = ?
                """, user.id(), registrationId);
        return Map.of(
                "registrationId", registrationId,
                "registrationStatus", "CHECKED_IN",
                "checkInTime", LocalDateTime.now().toString()
        );
    }

    private void validateCanGenerateCode(Map<String, Object> row, CurrentUser student) {
        if (((Number) row.get("student_id")).intValue() != student.id()) {
            throw new BusinessException(40301, "只能生成自己的签到码");
        }
        if (!"ENROLLED".equals(row.get("status"))) {
            throw new BusinessException(40903, "当前报名状态不可签到");
        }
    }

    private void validateCanCheckIn(Map<String, Object> row, CurrentUser user) {
        int organizerId = ((Number) row.get("organizer_id")).intValue();
        if (user.role() != Role.ADMIN && organizerId != user.id()) {
            throw new BusinessException(40301, "只能核销自己活动的签到");
        }
    }

    private int parseCode(String token) {
        if (token == null || !token.contains(".")) {
            throw new BusinessException(40001, "签到码格式错误");
        }
        String[] parts = token.split("\\.", 2);
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        if (!sign(payload).equals(parts[1])) {
            throw new BusinessException(40001, "签到码无效");
        }
        String[] values = payload.split(":");
        long expiresAt = Long.parseLong(values[1]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException(40903, "签到码已过期");
        }
        return Integer.parseInt(values[0]);
    }

    private String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("签名失败", ex);
        }
    }
}
