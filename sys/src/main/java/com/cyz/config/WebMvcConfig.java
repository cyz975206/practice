package com.cyz.config;

import com.cyz.repository.SysConfigRepository;
import com.cyz.security.JwtAuthenticationFilter;
import com.cyz.security.JwtTokenProvider;
import com.cyz.service.DictService;
import com.cyz.service.SecurityLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(
            JwtTokenProvider jwtTokenProvider,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            SysConfigRepository configRepository,
            SecurityLogService securityLogService,
            DictService dictService) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(
                jwtTokenProvider, redisTemplate, objectMapper, configRepository, securityLogService, dictService));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
