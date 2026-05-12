package com.spring.boot.gateway.config;

import com.spring.boot.commoncore.result.*;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/9 14:35
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentinelConfig {

	private final ObjectMapper objectMapper;
	@PostConstruct
	public void init() {
		GatewayCallbackManager.setBlockHandler((exchange, t) -> {

			// 注：Spring Cloud Gateway 的 Sentinel 适配中，QPS 限流和热点参数限流最终都会包装成 ParamFlowException，
			//     授权规则则抛出 AuthorityException。此处动态获取异常类型，适配后续扩展。
			log.warn("【网关】Sentinel拦截，路径：{}，异常类型：{}",
					exchange.getRequest().getURI().getPath(),
					t.getClass().getSimpleName());

			Result<Void> result = Result.fail(ResultCode.GATEWAY_RATE_LIMIT);
			return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(result);
		});
	}
}
