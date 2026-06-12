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
 * 网关全局认证过滤器
 * <p>
 * 负责对所有经过网关的请求进行统一鉴权处理：
 * 1. 记录请求日志（路径、方法、来源IP）
 * 2. 处理跨域预检请求（OPTIONS）
 * 3. 放行白名单路径（无需认证）
 * 4. 验证JWT Token的有效性（缺失、黑名单、过期等）
 * 5. 将解析出的用户ID传递给下游微服务
 * 6. 记录响应日志（耗时、状态码）
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/9 11:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	// Ant风格路径匹配器，用于白名单路径匹配（支持通配符如 /**）
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	// Jackson对象映射器，用于将Result对象序列化为JSON字节数组
	private final ObjectMapper objectMapper;

	// JWT工具类，用于Token的解析和验证
	@Autowired
	private JwtUtil jwtUtil;


	/**
	 * 白名单路径列表
	 * <p>
	 * 这些路径不需要进行JWT认证，直接放行：
	 * - 用户登录、注册、登出、Token刷新接口
	 * - Swagger/Knife4j 接口文档相关资源
	 * - 静态资源文件
	 */
	private static final List<String> WHITE_LIST = List.of(
			// 用户服务接口
			"/user-service/user/login",      // 用户登录
			"/user-service/user/register",   // 用户注册
			"/user-service/user/logout",     // 用户登出
			"/user-service/user/refresh",    // Token刷新
			// Knife4j/Swagger 文档资源
			"/doc.html",                     // Knife4j文档页面
			"/webjars/**",                   // Webjars静态资源
			"/favicon.ico",                  // 网站图标
			// API 文档接口 - 覆盖所有服务前缀和子路径
			"/**/v3/api-docs",
			"/**/v3/api-docs/**",            // OpenAPI文档接口
			"/**/swagger-resources",
			"/**/swagger-resources/**",      // Swagger资源配置
			"/**/swagger-ui/**",             // Swagger UI界面资源
			"/**/swagger-ui.html"           // Swagger UI页面
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
			log.debug("【网关】白名单路径放行: {}", path);
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
		} catch (IllegalStateException e) {
			log.warn("【网关】token在黑名单中，路径：{}，IP：{}", path, ip);
			return writeResponse(exchange, Result.fail(ResultCode.GATEWAY_TOKEN_BLACKLISTED));
		} catch (Exception e) {
			log.warn("【网关】token无效或过期，路径={}，IP={}，原因={}", path, ip, e.getMessage());
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