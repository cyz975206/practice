package com.cyz.seal.iam.infrastructure.security;

import com.cyz.seal.iam.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 签发/解析（jjwt 0.12.6）。
 *
 * <p>claim：subject=userId，外加 username / legalEntityId / roleCodes。
 */
@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Long userId, String username, Long legalEntityId, List<String> roleCodes) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + props.getExpiry().toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("legalEntityId", legalEntityId)
                .claim("roleCodes", roleCodes)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** 解析并校验 token；非法/过期会抛 JwtException。 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
