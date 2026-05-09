package com.spring.boot.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/9 11:30
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 白名单路径，不需要鉴权
	 */
	private static final List<String> WHITE_LIST = List.of(
			"/user/login",
			"/user/register",
			"/doc.html",
			"/webjars/**",
			"/favicon.ico",
			"/user-service/v3/api-docs/**",
			"/order-service/v3/api-docs/**",
			"/stock-service/v3/api-docs/**",
			"/swagger-resources/**",
			"/swagger-ui/**"
	);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Instant start = Instant.now();
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();
		String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
		String ip = getClientIp(request);

		// 1. 日志：记录请求信息
		log.info("【网关】请求 {} {}，来源IP：{}", method, path, ip);

		// 2. 跨域：添加 CORS 响应头
		exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
		exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
		exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

		// 3. 鉴权：白名单放行，其余检查 token
		if (!isWhiteListed(path)) {
			String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if (token == null || token.isBlank()) {
				log.warn("【网关】鉴权失败，缺少token，路径：{}，IP：{}", path, ip);
				exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}
			// TODO: 后续接入 JWT 校验逻辑，验证 token 有效性
			log.debug("【网关】token校验通过，路径：{}", path);
		}

		// 4. 继续执行，记录耗时
		return chain.filter(exchange).doFinally(signal -> {
			Duration duration = Duration.between(start, Instant.now());
			log.info("【网关】响应 {} {}，耗时：{}ms，状态码：{}",
					method, path, duration.toMillis(),
					exchange.getResponse().getStatusCode());
		});
	}

	@Override
	public int getOrder() {
		return -100;
	}

	/**
	 * 判断路径是否在白名单中
	 */
	private boolean isWhiteListed(String path) {
		return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	/**
	 * 获取客户端真实 IP
	 */
	private String getClientIp(ServerHttpRequest request) {
		String ip = request.getHeaders().getFirst("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = request.getHeaders().getFirst("X-Real-IP");
		}
		if (ip == null || ip.isBlank()) {
			ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
		}
		return ip;
	}
}
