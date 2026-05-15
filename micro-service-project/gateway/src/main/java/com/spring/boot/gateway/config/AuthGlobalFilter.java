package com.spring.boot.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.result.ResultCode;
import com.spring.boot.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final ObjectMapper objectMapper;

	@Autowired
	private JwtUtil jwtUtil;

	/**
	 * 白名单路径，不需要鉴权
	 */
	private static final List<String> WHITE_LIST = List.of(
			"/user-service/user/login",
			"/user-service/user/register",
			"/doc.html",
			"/webjars/**",
			"/favicon.ico",
			// Swagger/Knife4j - 无论哪个服务前缀都放行
			"/**/v3/api-docs/**",
			"/**/swagger-resources/**",
			"/**/swagger-ui/**",
			"/**/swagger-ui.html"
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

		// 2. 跨域（挪至yml）

		// 3. 鉴权：OPTIONS 和 白名单放行，其余检查 token
		if ("OPTIONS".equalsIgnoreCase(method) || isWhiteListed(path)) {
			return chain.filter(exchange);
		}

		// 检查 Token
		String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (token == null || token.isBlank()) {
			log.warn("【网关】鉴权失败，缺少token，路径：{}，IP：{}", path, ip);
			return writeResponse(exchange, Result.fail(ResultCode.GATEWAY_TOKEN_MISSING));
		}

		// 解析 Token，验证有效性
		Long userId;
		try {
			userId = jwtUtil.getUserIdFromToken(token);
		} catch (Exception e) {
			log.warn("【网关】token无效或过期，路径：{}，IP：{}", path, ip);
			return writeResponse(exchange, Result.fail(ResultCode.GATEWAY_TOKEN_EXPIRED));
		}

		// Token 有效，把 userId 传给下游
		ServerHttpRequest modifiedRequest = request.mutate()
				.header("X-UserId", String.valueOf(userId))
				.build();

		// 4. 继续执行，记录耗时
		return chain.filter(exchange.mutate().request(modifiedRequest).build())
				.doFinally(signal -> {
					Duration duration = Duration.between(start, Instant.now());
					HttpStatusCode code = exchange.getResponse().getStatusCode();
					log.info("【网关】响应 {} {}，耗时：{}ms，状态码：{}",
							method, path, duration.toMillis(),
							code != null ? code.value() : "UNKNOWN");
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

	/**
	 * 写入统一JSON响应
	 */
	private Mono<Void> writeResponse(ServerWebExchange exchange, Result<?> result) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(result))
				.map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
				.flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
				.onErrorResume(e -> {
					log.error("【网关】写入响应失败", e);
					return exchange.getResponse().setComplete();
				});
	}
}