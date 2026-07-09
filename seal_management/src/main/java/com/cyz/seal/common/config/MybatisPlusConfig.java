package com.cyz.seal.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.cyz.seal.common.context.LegalEntityContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis-Plus 配置：多租户行级隔离（ADR-0002）+ 分页。
 *
 * <p>多租户：每张业务表带 legal_entity_id，{@link TenantLineInnerInterceptor} 自动注入过滤条件；
 * Flowable 的 ACT_* 表与全局字典/菜单表在 {@link #GLOBAL_TABLES} / 前缀忽略。
 *
 * <p><b>无上下文 / group-scope 旁路</b>：getTenantId 在非请求线程返回 null，
 * 此时拦截器跳过注入——适合定时任务/异步；超级管理员/超级审计员的跨实体查询需走显式"忽略租户"路径
 *（GROUP 角色，单独审计，见 ADR-0002），由线程级开关触发——<b>已设计、待实现</b>。
 */
@Configuration
public class MybatisPlusConfig {

    /** 全局表（不参与行级隔离）：法人实体本身（租户单位）。其余全局表随上下文实现补充。 */
    private static final Set<String> GLOBAL_TABLES = Set.of(
            "legal_entity"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1) 多租户行级隔离
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long id = LegalEntityContext.get();
                return id == null ? null : new LongValue(id);
            }

            @Override
            public String getTenantIdColumn() {
                return "legal_entity_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                if (tableName == null) {
                    return true;
                }
                String t = tableName.toLowerCase();
                return t.startsWith("act_") || GLOBAL_TABLES.contains(t);
            }
        }));

        // 2) 分页（固定 PostgreSQL 方言，ADR-0001：本项目仅 PG / 信创 PG 系）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        return interceptor;
    }
}
