package com.spring.boot.userservice.util;

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
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 11:03
 */
@Component
public class JwtUtil {

	private final SecretKey key;
	private final long expire;

	@Autowired
	private StringRedisTemplate redisTemplate;

	public JwtUtil(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expire}") long expire) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expire = expire;
	}

	public String generateToken(Long userId) {
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expire))
				.signWith(key)
				.compact();
	}

	public void blacklistToken(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}

		Long userId = getUserIdFromToken(token);
		long remainingTime = getRemainingTime(token);

		redisTemplate.opsForValue().set(
				"blacklist:" + token,
				String.valueOf(userId),
				remainingTime,
				TimeUnit.MILLISECONDS
		);
	}

	// 工具方法
	private Long getUserIdFromToken(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		return parseToken(token);
	}

	private Long parseToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return Long.valueOf(claims.getSubject());
	}

	private long getRemainingTime(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			Date expiration = claims.getExpiration();
			return Math.max(0, expiration.getTime() - System.currentTimeMillis());
		} catch (Exception e) {
			return 0;
		}
	}
}
