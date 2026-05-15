package com.spring.boot.gateway.util;

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

	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public Long getUserIdFromToken(String token) {
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

}
