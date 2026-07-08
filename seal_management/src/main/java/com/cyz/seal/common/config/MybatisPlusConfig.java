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
 * <p><b>无上下文 / group-scope 旁路（TODO）</b>：getTenantId 在非请求线程返回 null，
 * 此时拦截器跳过注入——适合定时任务/异步；集团审计员/管理员的跨实体查询需走显式"忽略租户"路径
 *（仅 GROUP 角色，单独审计，见 ADR-0002），待 IAM 接入后用线程级开关实现。
 */
@Configuration
public class MybatisPlusConfig {

    /** 全局表（不参与行级隔离）：系统字典、菜单、角色、机构主数据等。按需补充。 */
    private static final Set<String> GLOBAL_TABLES = Set.of(
            "sys_dict_type", "sys_dict_item", "sys_menu", "sys_role", "sys_config"
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

        // 2) 分页（DbType 运行期由数据源决定；dev=H2，prod=PostgreSQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));

        return interceptor;
    }
}
