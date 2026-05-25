package com.spring.boot.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 11:03
 */
@Component
public class JwtUtil {

	private final SecretKey key;

	@Autowired
	private StringRedisTemplate redisTemplate;

	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public Long getUserIdFromToken(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		if (isTokenBlacklisted(token)) {
			throw new IllegalStateException("Token已在黑名单中");
		}
		return parseToken(token);
	}

	private boolean isTokenBlacklisted(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
	}

	private Long parseToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return Long.valueOf(claims.getSubject());
	}



}
