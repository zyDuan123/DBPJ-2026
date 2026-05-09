package com.campus.activity.common;

import java.util.Arrays;

public final class Access {
    private Access() {
    }

    public static CurrentUser require(Role... roles) {
        CurrentUser user = AuthContext.get();
        if (Arrays.stream(roles).noneMatch(role -> role == user.role())) {
            throw new BusinessException(40301, "当前角色无权限");
        }
        return user;
    }
}
