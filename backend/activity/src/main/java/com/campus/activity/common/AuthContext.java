package com.campus.activity.common;

public final class AuthContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(40101, "未登录或 Token 过期");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
