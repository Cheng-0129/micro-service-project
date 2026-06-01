package com.spring.boot.commonweb.aspect;

import com.spring.boot.commoncore.annotation.PreventReplay;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.ResultCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/19 11:36
 */
@Slf4j
@Aspect
@Component
public class ReplayAttackAspect {

	@Autowired
	private StringRedisTemplate redisTemplate;
	@Around("@annotation(preventReplay)")
	public Object around(ProceedingJoinPoint joinPoint, PreventReplay preventReplay) throws Throwable {

		log.debug("【防重放切面】进入切面，拦截方法：{}", joinPoint.getSignature().toShortString());

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return joinPoint.proceed();
		}

		HttpServletRequest request = attributes.getRequest();

		String nonce = request.getHeader("X-Nonce");
		String timestamp = request.getHeader("X-Timestamp");

		if (nonce == null || nonce.isBlank() || timestamp == null || timestamp.isBlank()) {
			log.warn("【防重放】缺少必要参数，nonce: {}, timestamp: {}", nonce, timestamp);
			throw BusinessException.of(ResultCode.REPLAY_ATTACK);
		}

		try {
			long requestTime = Long.parseLong(timestamp);
			long currentTime = System.currentTimeMillis();

			if (Math.abs(currentTime - requestTime) > preventReplay.timeout()) {
				log.warn("【防重放】请求超时，当前时间：{}，请求时间：{}", currentTime, requestTime);
				throw BusinessException.of(ResultCode.REPLAY_TIMEOUT);
			}

			String key = "replay:" + nonce;
			Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(key, "1", preventReplay.timeout(), TimeUnit.MILLISECONDS);

			if (Boolean.FALSE.equals(isFirst)) {
				log.warn("【防重放】重复请求，nonce：{}", nonce);
				throw BusinessException.of(ResultCode.REPLAY_DUPLICATE);
			}

			return joinPoint.proceed();

		} catch (BusinessException e) {
			throw e;
		} catch (NumberFormatException e) {
			log.error("【防重放】时间戳格式错误：{}", timestamp, e);
			throw BusinessException.of(ResultCode.REPLAY_TIMESTAMP_ERROR);
		}
	}
}
