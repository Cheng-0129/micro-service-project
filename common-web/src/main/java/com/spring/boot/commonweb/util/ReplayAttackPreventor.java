package com.spring.boot.commonweb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/19 11:17
 */
@Slf4j
@Component
public class ReplayAttackPreventor {

	@Autowired
	private StringRedisTemplate redisTemplate;

	// 1分钟的时间戳差值
	private static final long TIMESTAMP_THRESHOLD = 60000; // 1分钟

	public boolean validate(String nonce, String timestamp) {
		if (nonce == null || nonce.isBlank() || timestamp == null || timestamp.isBlank()) {
			return false;
		}

		try {
			long requestTime = Long.parseLong(timestamp);
			long currentTime = System.currentTimeMillis();

			if (Math.abs(currentTime - requestTime) > TIMESTAMP_THRESHOLD) {
				log.warn("请求超时，当前时间：{}，请求时间：{}", currentTime, requestTime);
				return false;
			}

			String key = "nonce:" + nonce;
			Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(key, "1", 2, TimeUnit.MINUTES);

			if (Boolean.FALSE.equals(isFirst)) {
				log.warn("重复的请求，nonce：{}", nonce);
				return false;
			}

			return true;
		} catch (NumberFormatException e) {
			log.error("时间戳格式错误：{}", timestamp, e);
			return false;
		}
	}
}
