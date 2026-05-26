package com.spring.boot.userservice.util;

import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.userservice.dto.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.spring.boot.commoncore.result.ResultCode.GATEWAY_TOKEN_EXPIRED;
import static com.spring.boot.commoncore.result.ResultCode.GATEWAY_TOKEN_INVALID;

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
	private final long accessExpire;   // Access Token 有效期（15分钟）
	private final long refreshExpire;  // Refresh Token 有效期（7天）

	@Autowired
	private StringRedisTemplate redisTemplate;

	public JwtUtil(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-expire}") long accessExpire,
			@Value("${jwt.refresh-expire}") long refreshExpire) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.accessExpire = accessExpire;
		this.refreshExpire = refreshExpire;
	}

	// 生成 Access Token
	public String generateAccessToken(Long userId) {
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("type", "access")  // ✅ 标记 Token 类型
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessExpire))
				.signWith(key)
				.compact();
	}

	// 生成 Refresh Token
	public String generateRefreshToken(Long userId) {
		String refreshToken = Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("type", "refresh")  // ✅ 标记 Token 类型
				.claim("jti", UUID.randomUUID().toString())  // ✅ 唯一标识
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshExpire))
				.signWith(key)
				.compact();

		// ✅ 将 Refresh Token 存入 Redis，用于轮换和吊销
		redisTemplate.opsForValue().set(
				"refresh_token:" + refreshToken,
				String.valueOf(userId),
				refreshExpire,
				TimeUnit.MILLISECONDS
		);

		// ✅ 维护用户与 Refresh Token 的映射（支持全局登出）
		redisTemplate.opsForSet().add(
				"user_refresh_tokens:" + userId,
				refreshToken
		);

		log.debug("【JWT】Refresh Token 已存入 Redis，用户ID={}", userId);

		return refreshToken;
	}

	// 刷新 Token（核心：轮换机制）
	public TokenPair refreshToken(String oldRefreshToken) {
		if (oldRefreshToken.startsWith("Bearer ")) {
			oldRefreshToken = oldRefreshToken.substring(7);
		}

		// 1. 解析 JWT
		Claims claims;
		try {
			claims = Jwts.parser().verifyWith(key).build()
					.parseSignedClaims(oldRefreshToken).getPayload();
		} catch (Exception e) {
			log.warn("【JWT】Refresh Token 解析失败，原因={}", e.getMessage());
			throw BusinessException.of(GATEWAY_TOKEN_INVALID);
		}

		// 2. 校验类型
		if (!"refresh".equals(claims.get("type"))) {
			log.warn("【JWT】刷新接口收到非 Refresh Token，type={}，userId={}",
					claims.get("type"), claims.getSubject());
			throw BusinessException.of(GATEWAY_TOKEN_INVALID);
		}
		Long userId = Long.valueOf(claims.getSubject());

		// 3. 验证 Refresh Token 是否在 Redis 中
		String cachedUserId = redisTemplate.opsForValue().get("refresh_token:" + oldRefreshToken);
		if (cachedUserId == null) {
			// ✅ 检测到重复使用，拉黑该用户所有 Token
			blacklistAllUserTokens(userId);
			log.warn("【JWT】检测到 Refresh Token 重复使用或已失效，用户ID: {}，已强制登出", userId);
			throw BusinessException.of(GATEWAY_TOKEN_EXPIRED);
		}

		// 4. 删除旧的 Refresh Token
		redisTemplate.delete("refresh_token:" + oldRefreshToken);
		redisTemplate.opsForSet().remove("user_refresh_tokens:" + userId, oldRefreshToken);

		// 5. 生成新的 Token Pair
		String newAccessToken = generateAccessToken(userId);
		String newRefreshToken = generateRefreshToken(userId);

		log.info("【JWT】Token 刷新成功，用户ID: {}", userId);
		return new TokenPair(newAccessToken, newRefreshToken);
	}

	// 全局登出：作废用户的所有 Refresh Token
	public void blacklistAllUserTokens(Long userId) {
		// 获取该用户的所有 Refresh Token
		Set<String> refreshTokens = redisTemplate.opsForSet().members("user_refresh_tokens:" + userId);

		if (refreshTokens != null) {
			for (String token : refreshTokens) {
				// 从 Redis 中删除
				redisTemplate.delete("refresh_token:" + token);
			}
		}
		// 清空用户的 Token 集合
		redisTemplate.delete("user_refresh_tokens:" + userId);
		log.info("【JWT】全局登出完成，共处理{}个Token，用户ID={}",
				refreshTokens != null ? refreshTokens.size() : 0, userId);
	}

	public void blacklistToken(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}

		Long userId = getUserIdFromToken(token);
		long remainingTime = Math.max(1000, getRemainingTime(token));

		redisTemplate.opsForValue().set(
				"blacklist:" + token,
				String.valueOf(userId),
				remainingTime,
				TimeUnit.MILLISECONDS
		);

		log.debug("【JWT】Token 已加入黑名单，用户ID={}，剩余有效时间={}ms", userId, remainingTime);
	}

	public void removeRefreshToken(String refreshToken) {
		if (refreshToken.startsWith("Bearer ")) {
			refreshToken = refreshToken.substring(7);
		}
		Long userId = parseToken(refreshToken);
		redisTemplate.delete("refresh_token:" + refreshToken);
		redisTemplate.opsForSet().remove("user_refresh_tokens:" + userId, refreshToken);
		log.info("【JWT】Refresh Token 已删除，用户ID={}", userId);
	}

	// 工具方法
	private Long getUserIdFromToken(String token) {
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		return parseToken(token);
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
			throw BusinessException.of(GATEWAY_TOKEN_INVALID);
		}
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
