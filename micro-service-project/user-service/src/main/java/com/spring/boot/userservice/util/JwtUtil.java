package com.spring.boot.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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
	private final long expire;

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
}
