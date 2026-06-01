package com.spring.boot.commonweb.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/19 14:23
 */
@Component
public class FeignReplayInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		template.header("X-Nonce", UUID.randomUUID().toString());
		template.header("X-Timestamp", String.valueOf(System.currentTimeMillis()));
	}
}
