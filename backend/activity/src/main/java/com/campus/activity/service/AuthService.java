package com.campus.activity.service;

import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.Role;
import com.campus.activity.model.entity.User;
import com.campus.activity.model.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final String secret;

    public AuthService(UserMapper userMapper, @Value("${app.auth-secret}") String secret) {
        this.userMapper = userMapper;
        this.secret = secret;
    }

    public CurrentUser authenticate(String username, String password) {
        User user = userMapper.authenticate(username, password);
        if (user == null) {
            throw new BusinessException(40101, "用户名或密码错误");
        }
        return toCurrentUser(user);
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
        User user = userMapper.findProfile(userId);
        if (user == null) {
            throw new BusinessException(40101, "未登录或 Token 过期");
        }
        return toCurrentUser(user);
    }

    private CurrentUser toCurrentUser(User user) {
        return new CurrentUser(
                user.getUserId(),
                user.getUsername(),
                Role.valueOf(user.getRole()),
                user.getStudentNo(),
                user.getPhone()
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
