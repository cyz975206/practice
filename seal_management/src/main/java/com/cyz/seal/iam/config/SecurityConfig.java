package com.cyz.seal.iam.config;

import com.cyz.seal.iam.infrastructure.security.JwtAuthenticationFilter;
import com.cyz.seal.iam.infrastructure.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：JWT（无状态）+ 白名单 + 其余 authenticated。
 *
 * <p>白名单：登录、系统探活、API 文档(knife4j/swagger)、actuator health。
 * 超级管理员不绕过多租户隔离（仅本实体）；GROUP 角色跨实体旁路随集团级角色后续实现。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // 开启方法级鉴权（@PreAuthorize），ADR：角色级粗粒度鉴权
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/system/ping",
                                "/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/webjars/**", "/favicon.ico",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未认证或登录已过期\",\"data\":null}");
                }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
