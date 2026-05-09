package com.campus.activity.common;

public record Result<T>(Integer code, String message, T data) {
    public static <T> Result<T> success(T data) {
        return new Result<>(20000, "success", data);
    }

    public static Result<Void> success() {
        return new Result<>(20000, "success", null);
    }

    public static Result<Void> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
