package com.cyz.security;

import com.cyz.config.JwtProperties;
import com.cyz.repository.SysConfigRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final SysConfigRepository configRepository;

    private static final long DEFAULT_EXPIRATION_MS = 20 * 60 * 1000L;

    public JwtTokenProvider(JwtProperties jwtProperties, SysConfigRepository configRepository) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.configRepository = configRepository;
    }

    public String generateToken(Long userId, String username) {
        long expirationMs = getExpirationMs();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public long getExpirationMs() {
        return configRepository.findByConfigKeyAndIsDeletedFalse("session_timeout_minutes")
                .map(config -> {
                    try {
                        int minutes = Integer.parseInt(config.getConfigValue());
                        return minutes > 0 ? minutes * 60L * 1000L : DEFAULT_EXPIRATION_MS;
                    } catch (NumberFormatException e) {
                        return DEFAULT_EXPIRATION_MS;
                    }
                })
                .orElse(DEFAULT_EXPIRATION_MS);
    }
}
