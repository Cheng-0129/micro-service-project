package com.spring.boot.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
			Long userId = parseTokenQuietly(token);
			log.warn("【网关JWT】Token 在黑名单中，userId={}", userId);
			throw new IllegalStateException("Token已在黑名单中");
		}
		Claims claims = Jwts.parser().verifyWith(key).build()
				.parseSignedClaims(token).getPayload();
		if (!"access".equals(claims.get("type"))) {
			throw new IllegalArgumentException("Token 类型错误，业务接口需要 Access Token");
		}
		return Long.valueOf(claims.getSubject());
	}

	private boolean isTokenBlacklisted(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		boolean blacklisted = Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
		if (blacklisted) {
			log.warn("【网关JWT】Token 在黑名单中，token前20位={}",
					token.substring(0, Math.min(20, token.length())));
		}
		return blacklisted;
	}

	private Long parseToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return Long.valueOf(claims.getSubject());
		} catch (Exception e) {
			log.warn("【JWT】Token 解析失败，原因={}，token前20位={}",
					e.getMessage(), token.substring(0, Math.min(20, token.length())));
			throw e;
		}
	}

	// 静默解析，不抛异常
	private Long parseTokenQuietly(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return Long.valueOf(claims.getSubject());
		} catch (Exception e) {
			return null;
		}
	}

}
