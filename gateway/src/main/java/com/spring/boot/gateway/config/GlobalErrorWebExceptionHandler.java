package com.spring.boot.gateway.config;

import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/9 11:01
 */
@Slf4j
@Order(-3)
@Configuration
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		Result<Void> result;

		// 404：不存在的路由或静态资源
		if (ex instanceof NoResourceFoundException) {
			String path = exchange.getRequest().getURI().getPath();
			log.warn("【网关】请求路径不存在：{}", path);
			exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
			exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
			result = Result.fail(ResultCode.GATEWAY_NOT_FOUND, "请求路径不存在：" + path);
			return serializeAndWrite(exchange, result);
		}

		if (ex instanceof BusinessException bizEx) {
			log.warn("【网关】业务异常：{}", bizEx.getMessage());
			result = Result.fail(bizEx.getCode(), bizEx.getMessage());
			exchange.getResponse().setStatusCode(HttpStatus.OK);
		} else {
			log.error("【网关】系统异常，原因={}", ex.getMessage(), ex);
			result = Result.fail(ResultCode.FAILED);
			exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		return serializeAndWrite(exchange, result);
	}

	private Mono<Void> serializeAndWrite(ServerWebExchange exchange, Result<Void> result) {
		return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(result))
				.map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
				.flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
				.onErrorResume(JsonProcessingException.class, e -> {
					log.error("【网关】响应序列化失败", e);
					exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
					return exchange.getResponse().setComplete();
				});
	}
}
