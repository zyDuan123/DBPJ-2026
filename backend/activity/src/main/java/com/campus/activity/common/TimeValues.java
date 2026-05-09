package com.campus.activity.common;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class TimeValues {
    private TimeValues() {
    }

    public static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof CharSequence text) {
            return LocalDateTime.parse(text.toString().replace(' ', 'T'));
        }
        throw new IllegalArgumentException("Unsupported datetime value: " + value);
    }
}
