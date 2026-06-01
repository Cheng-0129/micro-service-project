package com.spring.boot.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/19 14:28
 */
@Slf4j
@Component
public class ReplayAttackFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 给下游服务自动添加防重放参数
		ServerWebExchange modifiedExchange = exchange.mutate()
				.request(r -> r.header("X-Nonce", UUID.randomUUID().toString())
						.header("X-Timestamp", String.valueOf(System.currentTimeMillis())))
				.build();

		return chain.filter(modifiedExchange);
	}

	@Override
	public int getOrder() {
		return -200;
	}
}
