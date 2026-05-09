package com.campus.activity.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class AuthService {
    private final JdbcTemplate jdbcTemplate;
    private final String secret;

    public AuthService(JdbcTemplate jdbcTemplate, @Value("${app.auth-secret}") String secret) {
        this.jdbcTemplate = jdbcTemplate;
        this.secret = secret;
    }

    public CurrentUser authenticate(String username, String password) {
        var users = jdbcTemplate.queryForList("""
                SELECT user_id, username, role, student_no, phone
                FROM User
                WHERE (student_no = ? OR phone = ? OR username = ?) AND password = ?
                LIMIT 1
                """, username, username, username, password);
        if (users.isEmpty()) {
            throw new BusinessException(40101, "用户名或密码错误");
        }
        return toUser(users.get(0));
    }

    public String issueToken(CurrentUser user) {
        long expiresAt = Instant.now().plusSeconds(7 * 24 * 3600).getEpochSecond();
        String payload = user.id() + ":" + expiresAt;
        return base64(payload) + "." + sign(payload);
    }

    public CurrentUser parseToken(String token) {
        if (token == null || !token.contains(".")) {
            throw new BusinessException(40101, "未登录或 Token 过期");
        }
        String[] parts = token.split("\\.", 2);
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        if (!sign(payload).equals(parts[1])) {
            throw new BusinessException(40101, "Token 非法");
        }
        String[] values = payload.split(":");
        int userId = Integer.parseInt(values[0]);
        long expiresAt = Long.parseLong(values[1]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException(40101, "Token 已过期");
        }
        var user = jdbcTemplate.queryForMap("""
                SELECT user_id, username, role, student_no, phone
                FROM User
                WHERE user_id = ?
                """, userId);
        return toUser(user);
    }

    private CurrentUser toUser(Map<String, Object> row) {
        return new CurrentUser(
                ((Number) row.get("user_id")).intValue(),
                (String) row.get("username"),
                Role.valueOf((String) row.get("role")),
                (String) row.get("student_no"),
                (String) row.get("phone")
        );
    }

    private String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("签名失败", ex);
        }
    }
}
