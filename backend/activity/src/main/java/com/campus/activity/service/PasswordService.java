package com.campus.activity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
    private static final String PREFIX = "pbkdf2";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final int iterations;

    public PasswordService(@Value("${app.security.password.iterations:120000}") int iterations) {
        this.iterations = iterations;
    }

    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = derive(rawPassword, salt, iterations);
        return PREFIX + "$" + iterations + "$" + encode(salt) + "$" + encode(hash);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || !storedPassword.startsWith(PREFIX + "$")) {
            return false;
        }
        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        try {
            int storedIterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(rawPassword, salt, storedIterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derive(String rawPassword, byte[] salt, int iterationCount) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterationCount, HASH_BYTES * 8);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing failed", ex);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
