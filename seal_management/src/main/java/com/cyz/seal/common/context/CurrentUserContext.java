package com.cyz.seal.common.context;

/**
 * 当前登录用户 ID（请求级 ThreadLocal）：由 JWT 认证过滤器设置，
 * 供审计字段填充（{@code MyMetaObjectHandler}）等读取。
 *
 * <p>同 {@link LegalEntityContext}，仅覆盖请求线程；异步线程需另行传播。
 */
public final class CurrentUserContext {

    private static final ThreadLocal<Long> CTX = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(Long userId) {
        CTX.set(userId);
    }

    public static Long get() {
        return CTX.get();
    }

    public static void clear() {
        CTX.remove();
    }
}
