package com.cyz.seal.iam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT 配置（seal.jwt.*）。secret 务必在 application-*.yml 覆盖（HS256 需 ≥32 字节）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "seal.jwt")
public class JwtProperties {

    /** HMAC 密钥（≥32 字节）。 */
    private String secret = "seal-management-dev-jwt-secret-change-in-production-0123456789";

    /** 过期时间，默认 8 小时。 */
    private Duration expiry = Duration.ofHours(8);
}
