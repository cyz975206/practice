package com.cyz.security;

public class LoginUserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
