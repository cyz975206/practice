package com.cyz.seal.iam.infrastructure.security;

import com.cyz.seal.common.context.CurrentUserContext;
import com.cyz.seal.common.context.LegalEntityContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：校验 token → 重建登录态 → 激活多租户上下文（ADR-0002）+ 审计上下文。
 *
 * <p>非 @Component（避免被 Servlet 容器重复注册），由 {@code SecurityConfig} 显式加入过滤链。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            try {
                Claims claims = jwtUtil.parse(header.substring(BEARER.length()));
                Long userId = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);
                Long legalEntityId = claims.get("legalEntityId", Number.class).longValue();
                @SuppressWarnings("unchecked")
                List<String> roleCodes = (List<String>) claims.get("roleCodes");

                LoginUser principal = new LoginUser(userId, username, legalEntityId, roleCodes);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                LegalEntityContext.set(legalEntityId);   // 激活行级隔离
                CurrentUserContext.set(userId);          // 审计填充
            } catch (Exception ignored) {
                // token 无效/过期：不设上下文，受保护接口由 SecurityConfig 返回 401
                SecurityContextHolder.clearContext();
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // 防 ThreadLocal 跨请求泄漏（容器复用线程）
            LegalEntityContext.clear();
            CurrentUserContext.clear();
        }
    }
}
