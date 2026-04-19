package com.cyz.security;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.enums.RiskType;
import com.cyz.common.enums.RiskLevel;
import com.cyz.common.enums.HandleStatus;
import com.cyz.entity.SysSecurityLog;
import com.cyz.repository.SysConfigRepository;
import com.cyz.service.DictService;
import com.cyz.service.SecurityLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SysConfigRepository configRepository;
    private final SecurityLogService securityLogService;
    private final DictService dictService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String DEFAULT_WHITELIST = "/api/auth/login,/swagger-ui/**,/swagger-ui.html,/v3/api-docs/**,/favicon.ico,/error";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (isWhitelisted(path)) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(httpRequest);
        String clientIp = getClientIp(httpRequest);

        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            recordSecurityLog(null, null, clientIp, dictService.getDictValue(RiskType.class.getSimpleName(), RiskType.AUTH_FAIL.name()), "未登录或token已失效", path, dictService.getDictValue(RiskLevel.class.getSimpleName(), RiskLevel.LOW.name()));
            sendUnauthorized(httpResponse, "未登录或token已失效");
            return;
        }

        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(token);
        } catch (Exception e) {
            recordSecurityLog(null, null, clientIp, dictService.getDictValue(RiskType.class.getSimpleName(), RiskType.AUTH_FAIL.name()), "token解析失败", path, dictService.getDictValue(RiskLevel.class.getSimpleName(), RiskLevel.LOW.name()));
            sendUnauthorized(httpResponse, "未登录或token已失效");
            return;
        }

        String onlineKey = CacheConstant.ONLINE_USER_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(onlineKey);

        if (cached == null) {
            recordSecurityLog(userId, null, clientIp, dictService.getDictValue(RiskType.class.getSimpleName(), RiskType.SESSION_EXPIRED.name()), "会话已过期", path, dictService.getDictValue(RiskLevel.class.getSimpleName(), RiskLevel.LOW.name()));
            sendUnauthorized(httpResponse, "会话已过期，请重新登录");
            return;
        }

        LoginUser loginUser;
        try {
            String json = objectMapper.writeValueAsString(cached);
            loginUser = objectMapper.readValue(json, LoginUser.class);
        } catch (Exception e) {
            loginUser = (LoginUser) cached;
        }

        if (!token.equals(loginUser.getToken())) {
            recordSecurityLog(userId, loginUser.getUsername(), clientIp, dictService.getDictValue(RiskType.class.getSimpleName(), RiskType.SS_CONFLICT.name()),
                    "账号已在其他设备登录", path, dictService.getDictValue(RiskLevel.class.getSimpleName(), RiskLevel.MEDIUM.name()));
            sendUnauthorized(httpResponse, "账号已在其他设备登录");
            return;
        }

        LoginUserContext.set(loginUser);
        try {
            chain.doFilter(request, response);
        } finally {
            LoginUserContext.clear();
        }
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = loadWhitelist();
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private List<String> loadWhitelist() {
        try {
            Object cached = redisTemplate.opsForValue().get(CacheConstant.JWT_WHITELIST);
            if (cached instanceof String str && StringUtils.hasText(str)) {
                return parseWhitelist(str);
            }
        } catch (Exception e) {
            log.warn("读取白名单缓存失败", e);
        }

        String value = DEFAULT_WHITELIST;
        try {
            value = configRepository.findByConfigKeyAndIsDeletedFalse("jwt_whitelist")
                    .map(config -> StringUtils.hasText(config.getConfigValue()) ? config.getConfigValue() : DEFAULT_WHITELIST)
                    .orElse(DEFAULT_WHITELIST);
        } catch (Exception e) {
            log.warn("读取白名单配置失败，使用默认值", e);
        }

        try {
            redisTemplate.opsForValue().set(CacheConstant.JWT_WHITELIST, value, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存白名单失败", e);
        }

        return parseWhitelist(value);
    }

    private List<String> parseWhitelist(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private void recordSecurityLog(Long operatorId, String operatorName, String operatorIp,
                                     String riskType, String riskContent, String requestUrl, String riskLevel) {
        try {
            SysSecurityLog securityLog = new SysSecurityLog();
            securityLog.setOperatorId(operatorId);
            securityLog.setOperatorName(operatorName);
            securityLog.setOperatorIp(operatorIp);
            securityLog.setRiskType(riskType);
            securityLog.setRiskContent(riskContent);
            securityLog.setRequestUrl(requestUrl);
            securityLog.setRiskLevel(riskLevel);
            securityLog.setHandleStatus(dictService.getDictValue(HandleStatus.class.getSimpleName(), HandleStatus.UNHANDLED.name()));
            securityLog.setOccurTime(LocalDateTime.now());
            securityLogService.save(securityLog);
        } catch (Exception e) {
            log.warn("记录安全日志失败", e);
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
