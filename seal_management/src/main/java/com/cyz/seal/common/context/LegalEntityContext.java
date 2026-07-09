package com.cyz.seal.common.context;

/**
 * 请求级多租户上下文：持有当前请求所属的法人实体 ID（行级隔离，ADR-0002）。
 *
 * <p>由认证过滤器从 JWT claim 解析后 {@link #set(Long)}，请求结束时 {@link #clear()}。
 * {@code TenantLineInnerInterceptor} 读取之自动注入 legal_entity_id 过滤条件。
 *
 * <p><b>异步传播（已知缺口）</b>：基于 ThreadLocal，仅覆盖请求线程；XXL-JOB / RabbitMQ 消费者 /
 * Flowable async 等异步线程需显式 set/clear（或用 TaskDecorator 传播）。ADR-0009 分布式下需配套。
 */
public final class LegalEntityContext {

    private static final ThreadLocal<Long> CTX = new ThreadLocal<>();

    private LegalEntityContext() {
    }

    public static void set(Long legalEntityId) {
        CTX.set(legalEntityId);
    }

    /** @return 当前法人实体 ID；无上下文（非请求线程）时为 null。 */
    public static Long get() {
        return CTX.get();
    }

    public static void clear() {
        CTX.remove();
    }
}
