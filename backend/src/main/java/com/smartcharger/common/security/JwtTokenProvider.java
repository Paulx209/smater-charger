package com.smartcharger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "token:";

    public JwtTokenProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        // 存储token到Redis
        String redisKey = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, token, expiration, TimeUnit.MILLISECONDS);

        log.info("生成Token成功: userId={}, username={}", userId, username);
        return token;
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("从Token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("username", String.class);
        } catch (Exception e) {
            log.error("从Token中获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 1. 验证Token格式和签名
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            // 2. 验证Token是否在Redis中存在
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }

            String redisKey = TOKEN_PREFIX + userId;
            String redisToken = (String) redisTemplate.opsForValue().get(redisKey);

            // 3. 比对Redis中的token和传入的token是否一致
            return token.equals(redisToken);
        } catch (Exception e) {
            log.error("Token验证失败: ", e.getMessage());
            return false;
        }
    }

    /**
     * 删除Token（登出）
     */
    public void deleteToken(Long userId) {
        String redisKey = TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
        log.info("删除Token成功: userId={}", userId);
    }

    /**
     * 获取Token过期时间（毫秒）
     */
    public Long getExpiration() {
        return expiration;
    }
}
