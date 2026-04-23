package com.example.lipsticks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret; // 密钥

    @Value("${jwt.expiration-ms}")
    private long expirationMs; // 过期时间

    /**
     * 生成JWT Token
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expireAt)
                .claims(Map.of("role", role != null && !role.isBlank() ? role : "USER"))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? "USER" : String.valueOf(role);
    }

    public boolean isTokenValid(String token) {
        // 获取JWT Token的声明
        Claims claims = extractAllClaims(token);
        // 如果JWT Token已过期,则返回false
        return claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
