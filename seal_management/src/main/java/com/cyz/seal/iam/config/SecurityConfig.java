package com.cyz.seal.iam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置（骨架）。
 *
 * <p>骨架阶段放开所有接口便于联调。IAM 上下文接入后改为：
 * <ul>
 *   <li>JWT 认证过滤器（登录签发 JWT，请求校验）；</li>
 *   <li>RBAC（7 角色模型 + 资源权限 + 数据范围 ENTITY/GROUP，见 CONTEXT.md / ADR-0002）；</li>
 *   <li>白名单（/api/auth/login 等）；</li>
 *   <li>认证过滤器从 JWT claim 解析 legal_entity_id 写入 {@code LegalEntityContext}（行级隔离）。</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
